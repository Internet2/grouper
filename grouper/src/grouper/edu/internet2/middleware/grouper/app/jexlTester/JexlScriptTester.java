package edu.internet2.middleware.grouper.app.jexlTester;

import java.lang.ref.WeakReference;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyInput;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh.GrouperGroovyResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test jexl scripts
 */
public class JexlScriptTester {
  
  private static ThreadLocal<WeakReference<Object>> outputFromGshScript = new ThreadLocal<>();
  
  /**
   * @param scriptExample
   * @param availableBeansGshScript
   * @param nullCheckingJexlScript
   * @param jexlScript
   * @return jexl script result
   */
  public static JexlScriptTesterResult runJexlScript(ScriptExample scriptExample, String availableBeansGshScript, 
      String nullCheckingJexlScript, String jexlScript) {
    
   ScriptType scriptType = scriptExample.retrieveScriptType();
    
   StringBuilder overallGshScriptToExecute = new StringBuilder();
   overallGshScriptToExecute.append("import edu.internet2.middleware.grouper.app.jexlTester.*; \n");
   overallGshScriptToExecute.append(availableBeansGshScript);
   overallGshScriptToExecute.append("\nObject result = null; \n");
   
   if (StringUtils.isNotBlank(nullCheckingJexlScript)) {
     
     nullCheckingJexlScript = nullCheckingJexlScript.trim();
     
     nullCheckingJexlScript = StringEscapeUtils.escapeJava(nullCheckingJexlScript);
     
     overallGshScriptToExecute.append("String nullCheckingJexlScript =  \""+StringUtils.replace(nullCheckingJexlScript, "$", "\" + '$' + \"")  + "\"; \n");
     overallGshScriptToExecute.append("boolean shouldContinue = ScriptType."+ scriptType.name()+ ".nullCheckingShouldContinue(elVariableMap,  nullCheckingJexlScript); \n");
     overallGshScriptToExecute.append("if (shouldContinue) { \n ");
   }
   
   
   jexlScript = jexlScript.trim();
   jexlScript = StringEscapeUtils.escapeJava(jexlScript);
   overallGshScriptToExecute.append("String jexlScript =  \""+StringUtils.replace(jexlScript, "$", "\" + '$' + \"")  + "\"; \n");
   overallGshScriptToExecute.append("result = ScriptType."+ scriptType.name()+ ".runJexl(elVariableMap, jexlScript); \n");
   overallGshScriptToExecute.append("JexlScriptTester.registerOutput(result); \n");
   
   if (StringUtils.isNotBlank(nullCheckingJexlScript)) {
     overallGshScriptToExecute.append("} else { \n");
     overallGshScriptToExecute.append("JexlScriptTester.registerOutput(\"Null checking script returned false so the main JEXL script did not execute.\"); \n");
     overallGshScriptToExecute.append("} \n");
   }
   
   
   GrouperGroovyInput grouperGroovyInput = new GrouperGroovyInput();
   grouperGroovyInput.assignGrouperGroovyRuntime(new GrouperGroovyRuntime());
   grouperGroovyInput.assignUseTransaction(false);
   grouperGroovyInput.assignRunAsRoot(false);
   grouperGroovyInput.assignScript(overallGshScriptToExecute.toString());
   grouperGroovyInput.assignLightWeight(false);
   
   GrouperGroovyResult grouperGroovyResult = new GrouperGroovyResult();

   GrouperGroovysh.runScript(grouperGroovyInput, grouperGroovyResult);
   
   Integer resultCode = grouperGroovyResult.getResultCode();
   
   JexlScriptTesterResult result = new JexlScriptTesterResult();
   result.setGshScriptThatWasExecuted(overallGshScriptToExecute.toString());
   boolean success = GrouperUtil.intValue(resultCode, -1) == 0;
   if (success) {
     result.setResultForScreen(scriptType.resultForScreen());
   } else {
     if (grouperGroovyResult.getException() != null) {
       result.setResultForScreen(GrouperUtil.getFullStackTrace(grouperGroovyResult.getException()));
     }
   }
   result.setSuccess(success);
   
   return result;
   
  }
  
  public static void registerOutput(Object result) {
    outputFromGshScript.set(new WeakReference<Object>(result));
  } 
  
  public static Object retrieveOutputFromGshScript() {
    WeakReference<Object> weakReference = outputFromGshScript.get();
    if (weakReference != null) {
      return weakReference.get();
    }
    return null;
  }
  
}
