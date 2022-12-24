package edu.internet2.middleware.grouper.app.jexlTester;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public interface ScriptExample {
  
  public default String retrieveNullCheckingJexlScript() {
    String resourceName = "edu/internet2/middleware/grouper/app/jexlTester/jexlTesting_"+this.retrieveScriptType().name()+"__"+this.name()+"_nullChecking.jexl";
    return GrouperUtil.readResourceIntoString(resourceName, false);
  }

  public default String retrieveAvailableBeansGshScript() {
    String resourceName = "edu/internet2/middleware/grouper/app/jexlTester/jexlTesting_"+this.retrieveScriptType().name()+"__"+this.name()+"_availableBeans.gsh";
    return GrouperUtil.readResourceIntoString(resourceName, false);
  }
  
  public default String retrieveExampleJexlScript() {
    String resourceName = "edu/internet2/middleware/grouper/app/jexlTester/jexlTesting_"+this.retrieveScriptType().name()+"__"+this.name()+"_example.jexl";
    return GrouperUtil.readResourceIntoString(resourceName, false);
  }
  
  public ScriptType retrieveScriptType();
  
  public String name();

}
