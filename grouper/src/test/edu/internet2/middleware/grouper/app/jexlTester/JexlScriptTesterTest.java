package edu.internet2.middleware.grouper.app.jexlTester;

import edu.internet2.middleware.grouper.helper.GrouperTest;
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
        
        String availableBeansGshScript = scriptExample.retrieveAvailableBeansGshScript();
        String nullCheckingJexlScript = scriptExample.retrieveNullCheckingJexlScript();
        String exampleJexlScript = scriptExample.retrieveExampleJexlScript();
        
        JexlScriptTesterResult jexlScriptTesterResult = JexlScriptTester.runJexlScript(scriptExample, availableBeansGshScript, nullCheckingJexlScript, exampleJexlScript);
        
        assertEquals(true, jexlScriptTesterResult.isSuccess());
        
      }
      
    }
    
  }
  
}
