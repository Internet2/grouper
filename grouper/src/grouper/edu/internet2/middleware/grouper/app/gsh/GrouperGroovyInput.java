package edu.internet2.middleware.grouper.app.gsh;

import java.util.HashMap;
import java.util.Map;

public class GrouperGroovyInput {

  /**
   * pass in so you have a reference
   */
  private GrouperGroovyRuntime grouperGroovyRuntime = null;
  
  /**
   * pass in so you have a reference
   * @return
   */
  public GrouperGroovyRuntime getGrouperGroovyRuntime() {
    return grouperGroovyRuntime;
  }

  /**
   * pass in so you have a reference
   * @param grouperGroovyRuntime
   */
  public GrouperGroovyInput assignGrouperGroovyRuntime(GrouperGroovyRuntime grouperGroovyRuntime) {
    this.grouperGroovyRuntime = grouperGroovyRuntime;
    return this;
  }

  /**
   * if should run the script in transaction
   */
  private boolean useTransaction = false;
  
  /**
   * if should user transaction
   * @return true if should use transaction
   */
  public boolean isUseTransaction() {
    return useTransaction;
  }

  /**
   * if should use transaction
   * @param useTransaction
   */
  public GrouperGroovyInput assignUseTransaction(boolean useTransaction) {
    this.useTransaction = useTransaction;
    return this;
  }

  /**
   * script
   */
  private String script;
  
  /**
   * lightweight
   */
  private boolean lightWeight;

  /**
   * input name to value
   */
  private Map<String, Object> inputNameToValue = new HashMap<String, Object>();

  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;

  
  
  public Map<String, Object> getInputNameToValue() {
    return inputNameToValue;
  }


  public String getScript() {
    return script;
  }

  
  public GrouperGroovyInput assignScript(String script) {
    this.script = script;
    return this;
  }

  
  public boolean isLightWeight() {
    return lightWeight;
  }

  
  public GrouperGroovyInput assignLightWeight(boolean lightWeight) {
    this.lightWeight = lightWeight;
    return this;
  }


  /**
   * 
   * @param inputName
   * @param inputValue
   */
  public GrouperGroovyInput assignInputValueBoolean(String inputName, Boolean inputValue) {
    this.inputNameToValue.put(inputName, inputValue);
    return this;
  }


  /**
   * 
   * @param inputName
   * @param inputValue
   */
  public GrouperGroovyInput assignInputValueInteger(String inputName, Integer inputValue) {
    this.inputNameToValue.put(inputName, inputValue);
    return this;
  }


  /**
   * 
   * @param inputName
   * @param inputValue
   */
  public GrouperGroovyInput assignInputValueObject(String inputName, Object inputValue) {
    this.inputNameToValue.put(inputName, inputValue);
    return this;
  }


  /**
   * 
   * @param inputName
   * @param inputValue
   */
  public GrouperGroovyInput assignInputValueString(String inputName, String inputValue) {
    this.inputNameToValue.put(inputName, inputValue);
    return this;
  }

  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public GrouperGroovyInput assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }

  /**
   * set this to true to run as a root session
   * @return
   */
  public boolean isRunAsRoot() {
    return runAsRoot;
  }

  
  
  
}
