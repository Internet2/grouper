package edu.internet2.middleware.grouper.app.gsh.template;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateV2utils {

  public static Map<String, GshTemplateV2test> gshDiscoverTests(GshTemplateV2 gshTemplateV2) {

    Map<String, GshTemplateV2test> result = new TreeMap<>();
    Class theClass = gshTemplateV2.getClass();
    for (int i=0;i<10;i++) {

      Method[] methods = theClass.getDeclaredMethods();
      
      for (Method method : GrouperUtil.nonNull(methods, Method.class)) {

        // its not static
        if (Modifier.isStatic(method.getModifiers())) {
          continue;
        }

        if (!GshTemplateV2test.class.isAssignableFrom(method.getReturnType())) {
          continue;
        }
        
        if (method.getParameterCount() != 0) {
          continue;
        }
        
        if (!method.getName().toLowerCase().startsWith("test")) {
          continue;
        }
        
        GshTemplateV2test gshTemplateV2test = (GshTemplateV2test)GrouperUtil.callMethod(theClass, gshTemplateV2, method.getName());
        gshTemplateV2test.setName(method.getName());
        result.put(method.getName(), gshTemplateV2test);
        
      }
      
      theClass = theClass.getSuperclass();

      if (theClass.equals(GshTemplateV2.class)) {
        break;
      }

    }
    return result;
  }

  /**
   * 
   * @param testName
   */
  public static GshTemplateExecTestOutput gshRunTest(GshTemplateV2 gshTemplateV2, String testName) {
    
    Map<String, GshTemplateV2test> gshDiscoverTests = gshDiscoverTests(gshTemplateV2);
    GshTemplateV2test gshTemplateV2test = gshDiscoverTests.get(testName);
    GshTemplateV2testOutput gshTemplateV2testOutput = gshRunTest(gshTemplateV2, gshTemplateV2test, testName);
    
    GshTemplateExecTestOutput gshTemplateExecTestOutput = new GshTemplateExecTestOutput();
    GshTemplateV2utils.analyzeTestOutput(gshTemplateExecTestOutput, gshTemplateV2testOutput);
    return gshTemplateExecTestOutput;
  }
  
  /**
   * 
   * @param testName
   */
  public static GshTemplateV2testOutput gshRunTest(GshTemplateV2 gshTemplateV2, GshTemplateV2test gshTemplateV2test, String testName) {
    
    GrouperUtil.assertion(gshTemplateV2test != null, "Test cannot be found: '" + testName + "'");

    final GshTemplateV2testOutput gshTemplateV2testOutput = new GshTemplateV2testOutput();

    gshTemplateV2testOutput.setGshTemplateV2test(gshTemplateV2test);
    
    GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();

    GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();
    
    boolean[] validTest = new boolean[] {true};
    
    boolean startedSession = false;
    Subject grouperSessionPreviousSubject = GrouperSession.staticGrouperSession(false) == null ? null : GrouperSession.staticGrouperSession().getSubject();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    
    try {
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          gshTemplateV2test.setUp();
          return null;
        }
      });
    } catch (Throwable throwable) {
      gshTemplateV2testOutput.setThrowable(throwable);
      return gshTemplateV2testOutput;
    }
    gshTemplateV2testOutput.setSetupSuccess(true);
    gshTemplateRuntime.setWsInput(gshTemplateV2test.getGshWsInput());

    final GshTemplateOutput gshTemplateOutput = new GshTemplateOutput();
    gshTemplateV2testOutput.setGsh_builtin_gshTemplateOutput(gshTemplateOutput);

    GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();

    try {
    
      gshTemplateRuntime.setTemplateConfigId(gshTemplateV2test.getGshTemplateConfigId());
      gshTemplateV2input.setGsh_builtin_gshTemplateRuntime(gshTemplateRuntime);
  
      Subject subject = gshTemplateV2test.getGshSubjectUsingApp();
      if (subject == null) {
        GrouperUtil.assertion(GrouperSession.staticGrouperSession() != null, "Grouper session cannot be null");
        subject = GrouperSession.staticGrouperSession().getSubject();
      } else if (grouperSession == null || SubjectHelper.eq(subject, grouperSession.getSubject())) {
        startedSession = true;
        grouperSession = GrouperSession.start(subject);
      }
    
      gshTemplateV2input.setGsh_builtin_grouperSession(grouperSession);
      gshTemplateV2input.setGsh_builtin_ownerStemName(gshTemplateV2test.getGshStemName());
      gshTemplateV2input.setGsh_builtin_ownerGroupName(gshTemplateV2test.getGshGroupName());
  
      gshTemplateV2input.setGsh_builtin_subject(subject);
      gshTemplateV2input.setGsh_builtin_subjectId(subject.getId());

      Map<String, String> gshInputs = gshTemplateV2test.getGshInputs();
      
      GrouperUtil.assertion(!StringUtils.isBlank(gshTemplateV2test.getGshTemplateConfigId()), "Template config id is null, set it in the test setUp() method: this.setGshTemplateConfigId(\"someTemplateConfigId\");");
      
      GshTemplateConfig templateConfig = new GshTemplateConfig(gshTemplateV2test.getGshTemplateConfigId());
      templateConfig.populateConfiguration();
      
      GshTemplateOwnerType gshTemplateOwnerType = null;
      
      Stem ownerStem = null;
      if (!StringUtils.isBlank(gshTemplateV2test.getGshStemName())) {
        ownerStem = StemFinder.findByName(grouperSession, gshTemplateV2test.getGshStemName(), true);
        if (!templateConfig.canFolderRunTemplate(ownerStem)) {
          validTest[0] = false;
          gshTemplateV2testOutput.appendMessage("Cannot run template in folder: " + gshTemplateV2test.getGshStemName());
        }
        gshTemplateOwnerType = GshTemplateOwnerType.stem;
      }
      
      Group ownerGroup = null;
      if (!StringUtils.isBlank(gshTemplateV2test.getGshGroupName())) {
        ownerGroup = GroupFinder.findByName(grouperSession, gshTemplateV2test.getGshGroupName(), true);
        if (gshTemplateOwnerType != null) {
          validTest[0] = false;
          gshTemplateV2testOutput.appendMessage("Cannot set template test owner stem and group at same time: " + gshTemplateV2test.getName());
        } else if (!templateConfig.canGroupRunTemplate(ownerGroup)) {
          validTest[0] = false;
          gshTemplateV2testOutput.appendMessage("Cannot run template in group: " + gshTemplateV2test.getGshGroupName());
        }
        gshTemplateOwnerType = GshTemplateOwnerType.group;
      }
      
      GshTemplateExec gshTemplateExec = new GshTemplateExec();

      gshTemplateExec.assignWsInput(gshTemplateV2test.getGshWsInput());

      for (String gshInputName : gshInputs.keySet()) {
        String gshInputValue = gshInputs.get(gshInputName);
        
        if (gshInputName.startsWith("gsh_input_") && gshTemplateV2test.getGshWsInput().containsKey(gshInputName)) {
          gshInputValue = GrouperUtil.stringValue(gshTemplateV2test.getGshWsInput().get(gshInputName));
        }
        
        GshTemplateInput gshTemplateInput = new GshTemplateInput();
        gshTemplateInput.assignName(gshInputName);
        gshTemplateInput.assignValueString(gshInputValue);
        
        gshTemplateExec.addGshTemplateInput(gshTemplateInput);
        
        GshTemplateInputConfig gshTemplateInputConfig = templateConfig.retrieveGshTemplateInputConfig(gshInputName);
        Object realValue = gshTemplateInputConfig.getGshTemplateInputType().convertToType(gshInputValue);
        gshTemplateV2input.getGsh_builtin_inputs().put(gshInputName, realValue);
      }
            
      gshTemplateExec.assignConfigId(gshTemplateV2test.getGshTemplateConfigId());
      gshTemplateExec.assignCurrentUser(subject);
      gshTemplateExec.assignGshTemplateOwnerType(gshTemplateOwnerType);
      if (ownerGroup != null) {
        gshTemplateExec.assignOwnerGroupName(ownerGroup.getName());
      }
      if (ownerStem != null) {
        gshTemplateExec.assignOwnerStemName(ownerStem.getName());
      }
      
      gshTemplateV2output.setGsh_builtin_gshTemplateOutput(gshTemplateOutput);
      gshTemplateV2test.setGshTemplateOutput(gshTemplateOutput);
      if (validTest[0]) {
        if (new GshTemplateValidationService().validate(templateConfig, gshTemplateExec, gshTemplateOutput)) {
          gshTemplateV2.gshRunLogic(gshTemplateV2input, gshTemplateV2output);
        } else {
          validTest[0] = false;
        }
      }
      gshTemplateV2testOutput.setRunLogicSuccess(true);
    } catch (Throwable throwable) {
      gshTemplateV2testOutput.setThrowable(throwable);
    } finally {
      if (startedSession) {
        grouperSession.stop();
        if (grouperSessionPreviousSubject != null) {
          grouperSession = GrouperSession.start(grouperSessionPreviousSubject);
        }
      }
      if (gshTemplateV2test.isGshShowOutputLinesOfTest()) {
        for (GshOutputLine gshOutputLine : gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getOutputLines()) {
          gshTemplateV2testOutput.appendMessage("Template output: " + gshOutputLine);
        }
        for (GshValidationLine gshValidationLine : gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getValidationLines()) {
          gshTemplateV2testOutput.appendMessage("Template validation: " + gshValidationLine);
        }
      }
    }
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        try {
          if (gshTemplateV2testOutput.isRunLogicSuccess()) {
            boolean previouslyValidTest = validTest[0];
            validTest[0] = validTest[0] && GrouperUtil.length(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getValidationLines()) == 0;
            if (!gshTemplateV2test.isGshExpectValidationError() != validTest[0]) {
              gshTemplateV2testOutput.appendMessage("Expected validation error? " + gshTemplateV2test.isGshExpectValidationError()
                + ", validation error count: " + GrouperUtil.length(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getValidationLines())
                  + (GrouperUtil.length(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getValidationLines()) > 0 ? 
                      (", " + GrouperUtil.toStringForLogHtml(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getValidationLines())) : ""));
              gshTemplateV2testOutput.setFailure(true);
            }
            if (previouslyValidTest) {
              gshTemplateV2test.gshCheckResult();
            }
            gshTemplateV2testOutput.setCheckResultSuccess(true);
          }
        } catch (Throwable throwable) {
          gshTemplateV2testOutput.setThrowable(throwable);
        }
        try {
          if (gshTemplateV2testOutput.isSetupSuccess()) {
            gshTemplateV2test.tearDown();
            gshTemplateV2testOutput.setTearDownSuccess(true);
          }
        } catch (Throwable throwable) {
          if (gshTemplateV2testOutput.getThrowable() == null) {
            gshTemplateV2testOutput.setThrowable(throwable);
          }
          return null;
        }
        return null;
      }
    });
    return gshTemplateV2testOutput;
  }

  public static void analyzeTestOutput(
      GshTemplateExecTestOutput gshTemplateExecTestOutput,
      GshTemplateV2testOutput gshTemplateV2testOutput) {
    
    GshTemplateV2test gshTemplateV2test = gshTemplateV2testOutput.getGshTemplateV2test();
    gshTemplateExecTestOutput.addTest();
    
    if (gshTemplateV2testOutput.isSetupSuccess() && gshTemplateV2testOutput.isRunLogicSuccess() 
        && gshTemplateV2testOutput.isCheckResultSuccess() && gshTemplateV2testOutput.isTearDownSuccess()
        && gshTemplateV2testOutput.getThrowable() == null && !gshTemplateV2testOutput.isFailure()) {
      gshTemplateExecTestOutput.addSuccess();
      gshTemplateExecTestOutput.getGshTemplateOutput().addOutputLine("success", "Success: test '" + gshTemplateV2test.getName() + "'"
          + (StringUtils.isNotBlank(gshTemplateV2testOutput.getMessage()) ? (", " + gshTemplateV2testOutput.getMessage()) : ""));
    } else {
      if (gshTemplateV2testOutput.isSetupSuccess() && gshTemplateV2testOutput.isRunLogicSuccess() 
          && gshTemplateV2testOutput.isCheckResultSuccess() 
          && gshTemplateV2testOutput.isTearDownSuccess()) {
        if (gshTemplateV2testOutput.isFailure() || gshTemplateV2testOutput.getThrowable() instanceof AssertionError) {
          gshTemplateExecTestOutput.addFailure();
          gshTemplateExecTestOutput.getGshTemplateOutput().addOutputLine("error", "Failure: test '" + gshTemplateV2test.getName() + "'" 
              + (StringUtils.isNotBlank(gshTemplateV2testOutput.getMessage()) ? (", " + gshTemplateV2testOutput.getMessage()) : ""));
          
        } else {
          gshTemplateExecTestOutput.addException();
          gshTemplateExecTestOutput.getGshTemplateOutput().addOutputLine("error", "Exception: test '" + gshTemplateV2test.getName() + "'"
              + (StringUtils.isNotBlank(gshTemplateV2testOutput.getMessage()) ? (", " + gshTemplateV2testOutput.getMessage()) : ""));
          
        }
        
      } else {
        gshTemplateExecTestOutput.addInvalidTest();
        String section = "";
        if (!gshTemplateV2testOutput.isSetupSuccess()) {
          section = " in setUp()";
        } else if (!gshTemplateV2testOutput.isRunLogicSuccess()) {
          section = " in runLogic()";
        } else if (!gshTemplateV2testOutput.isCheckResultSuccess()) {
          section = " in gshCheckResult()";
        } else if (!gshTemplateV2testOutput.isTearDownSuccess()) {
          section = " in tearDown()";
        }
        
        gshTemplateExecTestOutput.getGshTemplateOutput().addOutputLine("error", "Invalid test" + section + ": '" + gshTemplateV2test.getName() + "'"
            + (StringUtils.isNotBlank(gshTemplateV2testOutput.getMessage()) ? (", " + gshTemplateV2testOutput.getMessage()) : ""));
      }
    } 
    if (gshTemplateV2testOutput.getThrowable() != null) {
      gshTemplateExecTestOutput.getGshTemplateOutput().addOutputLine("error", 
          GrouperUtil.getFullStackTraceHtml(gshTemplateV2testOutput.getThrowable()));
    }
    
  }

}
