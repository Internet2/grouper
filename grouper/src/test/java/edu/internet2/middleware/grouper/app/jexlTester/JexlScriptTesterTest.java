package edu.internet2.middleware.grouper.app.jexlTester;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

public class JexlScriptTesterTest extends GrouperTest {
  
  public JexlScriptTesterTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public JexlScriptTesterTest(String name) {
    super(name);
  }
  
  public static void main(String[] args) {
    TestRunner.run(new JexlScriptTesterTest("testRunJexlScript"));
  }

  
  public void testRunJexlScript() {
    
    for (ScriptType scriptType: ScriptType.values()) {
      
      Class<? extends ScriptExample> scriptExampleForTypeClass = scriptType.retrieveScriptExampleForType();
      
      ScriptExample[] enumConstants = scriptExampleForTypeClass.getEnumConstants();
      
      for (ScriptExample scriptExample: enumConstants) {
        try {
          String availableBeansGshScript = scriptExample.retrieveAvailableBeansGshScript();
          String nullCheckingJexlScript = scriptExample.retrieveNullCheckingJexlScript();
          String exampleJexlScript = scriptExample.retrieveExampleJexlScript();
          
          JexlScriptTesterResult jexlScriptTesterResult = JexlScriptTester.runJexlScript(scriptExample, availableBeansGshScript, nullCheckingJexlScript, exampleJexlScript);
          String resultString = null;
          if (scriptExample.expectedOutput() != null) {
            Object expectedOutput = scriptExample.expectedOutput();
            resultString = expectedOutput.getClass().getName() + ": " + GrouperUtil.stringValue(expectedOutput);
          } else {
            resultString = "<null>";
          }
          String resultForScreen = jexlScriptTesterResult.getResultForScreen();
          
          assertEquals(scriptExample + " error ", true, jexlScriptTesterResult.isSuccess());
          assertEquals(scriptExample + " error ", resultString, resultForScreen);
        } catch (RuntimeException e) {
          GrouperUtil.injectInException(e, scriptExample + " error ");
          throw e;
        }
        
      }
      
    }
    
  }
  
}
