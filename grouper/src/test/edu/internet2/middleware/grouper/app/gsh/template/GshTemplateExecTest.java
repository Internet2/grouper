package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasks;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang3.StringUtils;
import junit.textui.TestRunner;

public class GshTemplateExecTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateExecTest("testExecuteBasicGsh"));
    
//    GrouperStartup.startup();
//    GrouperSession.startRootSession();
//    
//    GshTemplateExec exec = new GshTemplateExec();
//    exec.assignConfigId("createNewWorkingGroup");
//    exec.assignCurrentUser(SubjectFinder.findRootSubject());
//    
//    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
//    exec.assignOwnerStemName("sandbox:ref:incommon-collab"); // run the script from test2 folder
//    
//    GshTemplateInput input = new GshTemplateInput();
//    input.assignName("gsh_input_workingGroupExtension");
//    input.assignValueString("myGroup");
//    exec.addGshTemplateInput(input);
//    input = new GshTemplateInput();
//    input.assignName("gsh_input_workingGroupDisplayExtension");
//    input.assignValueString("My group");
//    exec.addGshTemplateInput(input);
//    input = new GshTemplateInput();
//    input.assignName("gsh_input_workingGroupDescription");
//    input.assignValueString("My working group will do a lot of group work");
//    exec.addGshTemplateInput(input);
//    input = new GshTemplateInput();
//    input.assignName("gsh_input_isSympa");
//    input.assignValueString("true");
//    exec.addGshTemplateInput(input);
//    input = new GshTemplateInput();
//    input.assignName("gsh_input_sympaDomain");
//    input.assignValueString("internet2");
//    exec.addGshTemplateInput(input);
//    input = new GshTemplateInput();
//    input.assignName("gsh_input_isSympaModerated");
//    input.assignValueString("true");
//    exec.addGshTemplateInput(input);
//    //  input = new GshTemplateInput();
//    //  input.assignName("gsh_input_initialAdminSubjectId");
//    //  input.assignValueString("GrouperSys");
//    //  exec.addGshTemplateInput(input);
//    
//    // when
//    GshTemplateExecOutput output = exec.execute();
//    
//    // then
//    System.out.println("Success: " + output.isSuccess());
//    if (!output.isSuccess() && output.getException() != null) {
//      System.out.println(output.getExceptionStack());
//    }
//    System.out.println("Valid: " + output.isValid());
//    System.out.println("Validation:");
//    for (GshValidationLine gshValidationLine : output.getGshTemplateOutput().getValidationLines()) {
//      System.out.println(gshValidationLine.getInputName() + ": " + gshValidationLine.getText());
//    }
//    System.out.println("Output from script:");
//    for (GshOutputLine gshOutputLine : output.getGshTemplateOutput().getOutputLines()) {
//      System.out.println(gshOutputLine.getMessageType() + ": " + gshOutputLine.getText());
//    }
//    System.out.println("Script output:");
//    System.out.println(output.getGshScriptOutput());
    
    
  }
  
  public GshTemplateExecTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public GshTemplateExecTest(String name) {
    super(name);
  }
  
  public void testValidateRequiredInput() {
    
    // given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    Stem ownerStem = new StemSave(grouperSession).assignName("test2").save();
    
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("testGshTemplateConfig");
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(ownerStem.getName()); // run the script from test2 folder
    
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString(null);
    exec.addGshTemplateInput(input);
    
    
    // when
    GshTemplateExecOutput output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'Test label' is a required input field.", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
  }
  
  public void testValidateExtraInputThatIsNotInTemplate() {
    
    // given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    Stem ownerStem = new StemSave(grouperSession).assignName("test2").save();
    
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("testGshTemplateConfig");
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(ownerStem.getName()); // run the script from test2 folder
    
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("zoomTest");
    exec.addGshTemplateInput(input);
    
    GshTemplateInput extraInput = new GshTemplateInput();
    extraInput.assignName("extraInputName");
    extraInput.assignValueString("extraInputValue");
    exec.addGshTemplateInput(extraInput);
    
    
    // when
    GshTemplateExecOutput output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'extraInputName' does not exist in the input names configured in the template. Valid names are 'gsh_input_myExtension'.", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
  }
  
  public void testValidateOwnerStemRequiredWhenOwnerTypeIsStem() {
    
    // given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("testGshTemplateConfig");
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
   
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("zoomTest");
    exec.addGshTemplateInput(input);
    
    
    // when
    GshTemplateExecOutput output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("For gshTemplateOwnerType 'stem', ownerStemName cannot be blank.", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
    // when 
    exec.assignOwnerStemName("stemThatDoesNotExist");
    output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("Could not find ownerStem for name: 'stemThatDoesNotExist'.", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
  }
  
  public void testValidateOwnerGroupRequiredWhenOwnerTypeIsGroup() {
    
    // given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("testGshTemplateConfig");
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
    
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("zoomTest");
    exec.addGshTemplateInput(input);
    
    
    // when
    GshTemplateExecOutput output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("For gshTemplateOwnerType 'group', ownerGroupName cannot be blank.", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
    // when 
    exec.assignOwnerGroupName("groupThatDoesNotExist");
    output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("Could not find ownerGroup for name: 'groupThatDoesNotExist'.", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
  }
  
  public void testValidateInputValues() {
    
    // given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    Stem ownerStem = new StemSave(grouperSession).assignName("test2").save();
    
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("testGshTemplateConfig");
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(ownerStem.getName()); // run the script from test2 folder
    
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("notInt");
    exec.addGshTemplateInput(input);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.type", "integer");
    
    // when
    GshTemplateExecOutput output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'notInt' cannot be converted to 'integer'.", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
    // when input value does not meet regex pattern
    // given
    exec.getGshTemplateInputs().clear();
    input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("9");
    exec.addGshTemplateInput(input);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.validationType", "regex");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.validationRegex", "[0-8]+");
    
    // when
    output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'9' does not match regex '[0-8]+'.", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
  }
  
  public void testBuiltinValidations() {
    
    // given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    Stem ownerStem = new StemSave(grouperSession).assignName("test2").save();
    
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("testGshTemplateConfig");
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(ownerStem.getName()); // run the script from test2 folder
    
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("abcd8");
    exec.addGshTemplateInput(input);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.validationType", "builtin");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.validationBuiltin", "alpha");
    
    // when
    GshTemplateExecOutput output = exec.execute();
    
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'abcd8' contains non-alpha character(s).", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
    // given
    // builtin validation alphaNumeric
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.validationBuiltin", "alphaNumeric");
    exec.getGshTemplateInputs().clear();
    input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("abcd78_");
    exec.addGshTemplateInput(input);
    
    // when
    output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'abcd78_' contains non-alphanumeric character(s).", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
    // given
    // builtin validation alphaNumeric and underscore
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.validationBuiltin", "alphaNumericUnderscore");
    exec.getGshTemplateInputs().clear();
    input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("abcd78-");
    exec.addGshTemplateInput(input);
    
    // when
    output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'abcd78-' contains non-alphanumeric and underscore character(s).", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
    // given
    // builtin validation alphaNumeric and dash
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.validationBuiltin", "alphaNumericDash");
    exec.getGshTemplateInputs().clear();
    input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("abcd78_");
    exec.addGshTemplateInput(input);
    
    // when
    output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'abcd78_' contains non-alphanumeric and dash character(s).", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
    
    // given
    // builtin validation alphaNumeric, underscore, and dash
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.validationBuiltin", "alphaNumericUnderscoreDash");
    exec.getGshTemplateInputs().clear();
    input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("abcd78^");
    exec.addGshTemplateInput(input);
    
    // when
    output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'abcd78^' contains non-alphanumeric, underscore and dash character(s).", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
    // given
    // builtin validation noColons
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.validationBuiltin", "noColons");
    exec.getGshTemplateInputs().clear();
    input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("abcd78:abc");
    exec.addGshTemplateInput(input);
    
    // when
    output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'abcd78:abc' contains colon(s).", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
  }
  
  public void testValidateMaxLength() {
    
    // given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    Stem ownerStem = new StemSave(grouperSession).assignName("test2").save();
    
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("testGshTemplateConfig");
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(ownerStem.getName()); // run the script from test2 folder
    
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("abcdef");
    exec.addGshTemplateInput(input);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.maxLength", "5");
    
    // when
    GshTemplateExecOutput output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'Test label' cannot be more than '5' character(s).", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
  }
  
  public void testValidateDropdownValues() {
    
    // given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    Stem ownerStem = new StemSave(grouperSession).assignName("test2").save();
    
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("testGshTemplateConfig");
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(ownerStem.getName()); // run the script from test2 folder
    
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("abc");
    exec.addGshTemplateInput(input);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.formElementType", "dropdown");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.dropdownValueFormat", "csv");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.dropdownCsvValue", "def, ghi");
    
    // when
    GshTemplateExecOutput output = exec.execute();
    
    // then
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals(1, output.getGshTemplateOutput().getValidationLines().size());
    assertEquals("'Test label' is not valid. Valid values are 'def, ghi'.", output.getGshTemplateOutput().getValidationLines().get(0).getText());
    assertFalse(output.isSuccess());
    assertFalse(output.isValid());
    
  }

  public void testExecuteBasicGsh() {
    
    // given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    UpgradeTasks.V7.updateVersionFromPrevious();
    
    String folderUuidToShow = GrouperConfig.retrieveConfig().propertyValueString("grouperGshTemplate.testGshTemplateConfig.folderUuidToShow");
    assertEquals("abc,def", folderUuidToShow);
    // verify that in db, we have singular property 
    
    String gshScript = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-script2.gsh", false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.gshTemplate", gshScript);
    
    Stem ownerStem = new StemSave(grouperSession).assignName("test2").save();
    
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("testGshTemplateConfig");
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(ownerStem.getName()); // run the script from test2 folder
    
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValueString("zoomTest");
    exec.addGshTemplateInput(input);
    
    
    // when
    GshTemplateExecOutput output = exec.execute();
    
    // then
    Group groupCreatedByGsh = GroupFinder.findByName(grouperSession, "test:test-zoomTest", true);
    
    assertEquals(1, output.getGshTemplateOutput().getOutputLines().size());
    assertEquals("Created group: "+groupCreatedByGsh.getName(), output.getGshTemplateOutput().getOutputLines().get(0).getText());
    assertTrue(output.isSuccess());
    assertTrue(output.isValid());
    
  }
  
  public void testExecutePennGsh() {
    
    // given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    String gshScript = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-script-penn.gsh", false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.gshTemplate", gshScript);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate.testGshTemplateConfig.input.0.name", "gsh_input_prefix");
    
    Stem ownerStem = new StemSave(grouperSession).assignName("test2").save();
    
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("testGshTemplateConfig");
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(ownerStem.getName()); // run the script from test2 folder
    
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_prefix");
    input.assignValueString("TEST-Prefix");
    exec.addGshTemplateInput(input);
    
    Group usersExcludedFromZoomGroup = new GroupSave(grouperSession).assignName("penn:isc:ait:apps:zoom:service:ref:usersExcludedFromZoom").assignCreateParentStemsIfNotExist(true).save();
    Group lsps = new GroupSave(grouperSession).assignName("penn:isc:ait:apps:zoom:security:zoomSchoolCenterLspsPreCheck").assignCreateParentStemsIfNotExist(true).save();
    Group admins = new GroupSave(grouperSession).assignName("penn:isc:ait:apps:zoom:security:zoomSchoolCenterAdminsPreCheck").assignCreateParentStemsIfNotExist(true).save();
    
    
    // when
    GshTemplateExecOutput output = exec.execute();
    
    // then
    Group excludeAdHocGroup = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:zoom:service:ref:excludeAdHoc:test-prefixAdhocExcludeFromZoom", true);
    Group excludeLoadedGroup = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:test-prefixExcludeLoaded", true);
    Group excludeGroup = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:zoom:service:ref:excludeFromZoom:test-prefixExcludeFromZoom", true);
    Group schoolLspGroup = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomTEST-PrefixLsps", true);
    Group schoolAdminGroup = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomTEST-PrefixAdmins", true);
    
    assertEquals(2, excludeGroup.getMembers().size());
    
    assertEquals(1, usersExcludedFromZoomGroup.getImmediateMembers().size());
    assertEquals(3, usersExcludedFromZoomGroup.getMembers().size());
    assertEquals(excludeGroup.getName(), usersExcludedFromZoomGroup.getImmediateMembers().iterator().next().getName());
    
    assertEquals(1, lsps.getMembers().size());
    assertEquals(schoolLspGroup.getName(), lsps.getMembers().iterator().next().getName());
    
    assertEquals(1, admins.getMembers().size());
    assertEquals(schoolAdminGroup.getName(), admins.getMembers().iterator().next().getName());
    
    assertTrue(output.isSuccess());
    assertTrue(output.isValid());
    
  }


}
