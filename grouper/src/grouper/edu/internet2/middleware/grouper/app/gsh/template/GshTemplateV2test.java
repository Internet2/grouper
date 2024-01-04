package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.testing.GrouperTestInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * you can override to make a GSH test.  First setup() will be called.  Then runLogic() on template
 * @author mchyzer
 *
 */
public abstract class GshTemplateV2test extends GrouperTestInApi {

  private Map<String, Object> gshWsInput = new LinkedHashMap<>();
  
  public void setGshWsInput(Object wsInputBean) {
    if (wsInputBean == null) {
      gshWsInput = new LinkedHashMap<>();
    } else if (wsInputBean instanceof Map) {
      gshWsInput = (Map)wsInputBean;
    } else {
      String json = GrouperUtil.jsonConvertToNoWrap(wsInputBean);
      this.gshWsInput = GrouperUtil.jsonConvertFrom(json, Map.class);
    }
  }
  
  public Map<String, Object> getGshWsInput() {
    return gshWsInput;
  }

  /**
   * where template is called
   */
  private String gshStemName = null;

  /**
   * where template is called
   */
  private String gshGroupName = null;
  
  /**
   * where template is called
   * @return
   */
  public String getGshStemName() {
    return gshStemName;
  }

  /**
   * where template is called
   * @param gshStemName
   */
  public void setGshStemName(String gshStemName) {
    this.gshStemName = gshStemName;
  }

  /**
   * where template is called
   * @return
   */
  public String getGshGroupName() {
    return gshGroupName;
  }

  /**
   * where template is called
   * @param gshGroupName
   */
  public void setGshGroupName(String gshGroupName) {
    this.gshGroupName = gshGroupName;
  }

  /**
   * if the template depends on a certain config id, set it here, otherwise will be set for you
   */
  private String gshTemplateConfigId = null;
  
  /**
   * if the template depends on a certain config id, set it here, otherwise will be set for you
   * @return
   */
  public String getGshTemplateConfigId() {
    return gshTemplateConfigId;
  }

  /**
   * if the template depends on a certain config id, set it here, otherwise will be set for you
   * @param gshTemplateConfigId
   */
  public void setGshTemplateConfigId(String gshTemplateConfigId) {
    this.gshTemplateConfigId = gshTemplateConfigId;
  }

  /**
   * used for assertions
   */
  private GshTemplateOutput gshTemplateOutput;
  
  /**
   * used for assertions, teh GSH template framework sets this
   * @param gshTemplateOutput
   */
  public void setGshTemplateOutput(GshTemplateOutput gshTemplateOutput) {
    this.gshTemplateOutput = gshTemplateOutput;
  }

  /**
   * used for assertions, teh GSH template framework sets this
   * @return
   */
  public GshTemplateOutput getGshTemplateOutput() {
    return gshTemplateOutput;
  }

  /**
   * show output of template by default (default true)
   */
  private boolean gshShowOutputLinesOfTest = false;

  /**
   * show output of template by default (default true)
   * @return
   */
  public boolean isGshShowOutputLinesOfTest() {
    return gshShowOutputLinesOfTest;
  }

  /**
   * show output of template by default (default true)
   * @param gshShowOutputLinesOfTest
   */
  public void setGshShowOutputLinesOfTest(boolean gshShowOutputLinesOfTest1) {
    this.gshShowOutputLinesOfTest = gshShowOutputLinesOfTest1;
  }


  protected void setUp() {
  }

  protected void tearDown() {
  }

  /**
   * name should be set by test, otherwise it will be generated
   */
  private String name;

  /**
   * name should be set by test, otherwise it will be generated
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * name should be set by test, otherwise it will be generated
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  private Map<String, String> gshInputs = new LinkedHashMap<>();
  
  public void addGshInput(String inputName, String valueFromScreen) {
    this.gshInputs.put(inputName, valueFromScreen);
  }
  
  /**
   * return the string values of what it typed on screen.  Note: validations will be checked
   * @return
   */
  public Map<String, String> getGshInputs() {
    return gshInputs;
  }
  
  /**
   * true if a validation error is expected based on these inputs. default false
   */
  private boolean gshExpectValidationError = false;
  
  /**
   * true if a validation error is expected based on these inputs. default false
   * @return
   */
  public boolean isGshExpectValidationError() {
    return gshExpectValidationError;
  }

  /**
   * true if a validation error is expected based on these inputs. default false
   * @param gshExpectValidationError
   */
  public void setGshExpectValidationError(boolean gshExpectValidationError) {
    this.gshExpectValidationError = gshExpectValidationError;
  }

  /**
   * subject using the app in the test (all audits will be as that user).  or null will use the user running the test.
   */
  private Subject gshSubjectUsingApp;
  
  /**
   * return the subject using the app in the test (all audits will be as that user).  or null will use the user running the test.
   * @return the subject.
   */
  public Subject getGshSubjectUsingApp() {
    return this.gshSubjectUsingApp;
  }
  
  /**
   * subject using the app in the test (all audits will be as that user).  or null will use the user running the test.
   * @param gshSubjectUsingApp1
   */
  public void setGshSubjectUsingApp(Subject gshSubjectUsingApp1) {
    this.gshSubjectUsingApp = gshSubjectUsingApp1;
  }

  /**
   * subject using the app in the test (all audits will be as that user).  or null will use the user running the test.
   * @param source
   * @param subjectIdOrIdentifier
   */
  public void assignGshSubjectUsingApp(String source, String subjectIdOrIdentifier) {
    this.setGshSubjectUsingApp(SubjectFinder.findByIdOrIdentifierAndSource(subjectIdOrIdentifier, source, true));

  }

  /**
   * put logic here to check state of registry after the template is run
   */
  public void gshCheckResult() {
    
  }
  
  /**
   * see if there is an output matching this line
   * @param line to find
   * @param messageType success (default), info, error
   */
  public void assertGshOutputContainsLine(String messageType, String line) {
    if (gshOutputContainsLine(messageType, line)) {
      return;
    }
    fail("Cannot find line: '" + line + "'" + (StringUtils.isBlank(messageType) ? "" : (", messageType: " + messageType)) + " in output: " + GrouperUtil.toStringForLog(gshTemplateOutput.getOutputLines()));
  }

  /**
   * see if there is not an output matching this line
   * @param line to find
   * @param messageType success (default), info, error
   */
  public void assertGshOutputContainsLineNot(String messageType, String line) {
    if (!gshOutputContainsLine(messageType, line)) {
      return;
    }
    fail("Can find line: '" + line + "'" + (StringUtils.isBlank(messageType) ? "" : (", messageType: " + messageType)) + " in output (but shouldnt): " + GrouperUtil.toStringForLog(gshTemplateOutput.getOutputLines()));
  }

  /**
   * see if there is an output matching this line
   * @param line to find
   * @param messageType success (default), info, error
   */
  public boolean gshOutputContainsLine(String messageType, String line) {
    for (GshOutputLine gshOutputLine : GrouperUtil.nonNull(gshTemplateOutput.getOutputLines())) {
      if (messageType != null && !StringUtils.equals(messageType, StringUtils.defaultString(gshOutputLine.getMessageType(), "success"))) {
        continue;
      }
      if (StringUtils.equals(line, gshOutputLine.getText())) {
        return true;
      }
    }
    return false;
  }

  /**
   * see if there is an output matching this line
   * @param line to find
   */
  public void assertGshOutputContainsLine(String line) {
    assertGshOutputContainsLine(null, line);
  }

  /**
   * see if there is not an output matching this line
   * @param line to find
   */
  public void assertGshOutputContainsLineNot(String line) {
    assertGshOutputContainsLineNot(null, line);
  }

  /**
   * see if there is an output matching this line
   * @param substring to find
   */
  public void assertGshOutputLineContainsText(String messageType, String substring) {
    if (gshOutputLineContainsText(messageType, substring)) {
      return;
    }
    fail("Cannot find substring: '" + substring + "'" + (StringUtils.isBlank(messageType) ? "" : (", messageType: " + messageType)) + " in output: " + GrouperUtil.toStringForLog(gshTemplateOutput.getOutputLines()));
  }

  /**
   * see if there is not an output matching this line
   * @param substring to find
   */
  public void assertGshOutputLineContainsTextNot(String messageType, String substring) {
    if (!gshOutputLineContainsText(messageType, substring)) {
      return;
    }
    fail("Can find substring: '" + substring + "'" + (StringUtils.isBlank(messageType) ? "" : (", messageType: " + messageType)) + " in output (but shouldn't): " + GrouperUtil.toStringForLog(gshTemplateOutput.getOutputLines()));
  }

  /**
   * see if there is an output matching this line
   * @param substring to find
   */
  public boolean gshOutputLineContainsText(String messageType, String substring) {
    for (GshOutputLine gshOutputLine : GrouperUtil.nonNull(gshTemplateOutput.getOutputLines())) {
      if (messageType != null && !StringUtils.equals(messageType, StringUtils.defaultString(gshOutputLine.getMessageType(), "success"))) {
        continue;
      }
      if (GrouperUtil.defaultString(gshOutputLine.getText()).contains(substring)) {
        return true;
      }
    }
    return false;
  }

  /**
   * see if there is not an output matching this line
   * @param substring to find
   */
  public void assertGshOutputLineContainsTextNot(String substring) {
    assertGshOutputLineContainsTextNot(null, substring);
  }

  /**
   * see if there is an output matching this line
   * @param substring to find
   */
  public void assertGshOutputLineContainsText(String substring) {
    assertGshOutputLineContainsText(null, substring);
  }

  /**
   * see if there is an validation matching this line
   * @param line to find
   * @param inputName
   */
  public void assertGshValidationContainsLine(String inputName, String line) {
    if (gshValidationContainsLine(inputName, line)) {
      return;
    }
    fail("Cannot find line: '" + line + "', inputName: " + inputName + " in validation: " + GrouperUtil.toStringForLog(gshTemplateOutput.getValidationLines()));
  }

  /**
   * see if there is not an validation matching this line
   * @param line to find
   * @param inputName
   */
  public void assertGshValidationContainsLineNot(String inputName, String line) {
    if (!gshValidationContainsLine(inputName, line)) {
      return;
    }
    fail("Can find line: '" + line + "', inputName: " + inputName + " in validation (but shouldn't): " + GrouperUtil.toStringForLog(gshTemplateOutput.getValidationLines()));
  }

  /**
   * see if there is an validation matching this line
   * @param line to find
   * @param inputName
   */
  public boolean gshValidationContainsLine(String inputName, String line) {
    for (GshValidationLine gshValidationLine : GrouperUtil.nonNull(gshTemplateOutput.getValidationLines())) {
      if (!StringUtils.equals(inputName, gshValidationLine.getInputName())) {
        continue;
      }
      if (StringUtils.equals(line, gshValidationLine.getText())) {
        return true;
      }
    }
    return false;
  }

  /**
   * see if there is an validation matching this line
   * @param substring to find
   * @param inputName
   */
  public void assertGshValidationLineContainsText(String inputName, String substring) {
    if (gshValidationLineContainsText(inputName, substring)) {
      return;
    }
    fail("Cannot find substring: '" + substring + "', inputName: " + inputName + " in validation: " + GrouperUtil.toStringForLog(gshTemplateOutput.getValidationLines()));
  }

  /**
   * see if there is not a validation matching this line
   * @param substring to find
   * @param inputName
   */
  public void assertGshValidationLineContainsTextNot(String inputName, String substring) {
    if (!gshValidationLineContainsText(inputName, substring)) {
      return;
    }
    fail("Can find substring: '" + substring + "', inputName: " + inputName + " in validation (but shouldn't): " + GrouperUtil.toStringForLog(gshTemplateOutput.getValidationLines()));
  }

  /**
   * see if there is an validation matching this line
   * @param substring to find
   * @param inputName
   */
  public boolean gshValidationLineContainsText(String inputName, String substring) {
    for (GshValidationLine gshValidationLine : GrouperUtil.nonNull(gshTemplateOutput.getValidationLines())) {
      if (!StringUtils.equals(inputName, gshValidationLine.getInputName())) {
        continue;
      }
      if (GrouperUtil.defaultString(gshValidationLine.getText()).contains(substring)) {
        return true;
      }
    }
    return false;
  }


}
