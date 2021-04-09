package edu.internet2.middleware.grouper.app.gsh;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;

public class GrouperGroovyRuntime {

  private int resultCode = 0;
  
  public int getResultCode() {
    return resultCode;
  }

  
  public void setResultCode(int resultCode) {
    this.resultCode = resultCode;
  }

  /**
   * debug map
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

  
  public void setInputNameToValue(Map<String, Object> inputNameToValue) {
    this.inputNameToValue = inputNameToValue;
  }

  /**
   * name value pairs to log
   */
  public void debugMap(String key, Object value) {
    this.debugMap.put(key, value);
  }

  /**
   * name value pairs to log
   * @return
   */
  public Map<String, Object> getDebugMap() {
    return debugMap;
  }
  
  public StringBuilder getOutString() {
    return outString;
  }

  private StringBuilder outString = new StringBuilder();
  
  /**
   * print something to output
   * @param line
   */
  public void print(String line) {
    this.outString.append(line);
  }
  
  /**
   * print something to output with line at end
   * @param line
   */
  public void println(String line) {
    this.outString.append(line).append("\n");
  }
  
  private static ThreadLocal<GrouperGroovyRuntime> threadLocalGrouperGroovyRuntime = new InheritableThreadLocal<GrouperGroovyRuntime>();

  private GrouperSession grouperSession;
  
  public GrouperSession getGrouperSession() {
    return grouperSession;
  }

  private int percentDone = -1;
  
  private long startMillis1970 = -1;
  
  public void resetStartMillis1970() {
    this.startMillis1970 = System.currentTimeMillis();
  }
  
  /**
   * 
   */
  private int averageExecutionTimeMillis = -1;
  
  
  public int getAverageExecutionTimeMillis() {
    return averageExecutionTimeMillis;
  }

  
  public void setAverageExecutionTimeMillis(int averageExecutionTimeMillis) {
    this.averageExecutionTimeMillis = averageExecutionTimeMillis;
  }

  /**
   * 
   * @param millisElapsed
   * @return guess of percent done, dont go over 99
   */
  public int percentDone() {

    // if the script set it use that
    if (this.percentDone != -1) {
      return this.percentDone;
    }

    if (this.averageExecutionTimeMillis <= 0) {
      return 50;
    }
    
    long millisElapsed = System.currentTimeMillis() - this.startMillis1970;

    if (millisElapsed >= this.averageExecutionTimeMillis) {
      return 99;
    }
    
    // get the percentage
    int result = (int)((millisElapsed * 100.0f) / this.averageExecutionTimeMillis);
    
    return Math.min(result, 99);
    
  }
  
  public void setPercentDone(int percentDone) {
    this.percentDone = percentDone;
  }

  /**
   * input name to value
   */
  private Map<String, Object> inputNameToValue = null;

  public GrouperGroovyRuntime() {
    // TODO Auto-generated constructor stub
  }

  public void assignThreadLocalGrouperGroovyRuntime() {
    threadLocalGrouperGroovyRuntime.set(this);
  }

  public static void removeThreadLocalGrouperGroovyRuntime() {
    threadLocalGrouperGroovyRuntime.remove();
  }

  public static GrouperGroovyRuntime retrieveGrouperGroovyRuntime() {
    return threadLocalGrouperGroovyRuntime.get();
  }

  /**
   * 
   * @param inputName
   * @return the boolean value of the integer
   */
  public Boolean retrieveInputValueBoolean(String inputName) {
    
    Object inputValue = this.retrieveInputValueObject(inputName);
    if (inputValue == null) {
      return null;
    }
    
    if (!(inputValue instanceof Boolean)) {
      throw new RuntimeException("Expecting boolean but was: " + inputValue.getClass() + " for " + inputName + ": '" + inputValue + "'");
    }
    
    return (Boolean)inputValue;
  }

  /**
   * 
   * @param inputName
   * @return the value of the bind variable
   */
  public Object retrieveInputValueObject(String inputName) {
    
    if (!this.inputNameToValue.containsKey(inputName)) {
      throw new RuntimeException("Cant find input: '" + inputName + "'");
    }
    
    Object inputValue = this.inputNameToValue.get(inputName);
    return inputValue;
  }

  /**
   * 
   * @param inputName
   * @return the integer value of the integer
   */
  public Integer retrieveInputValueInteger(String inputName) {
    
    Object inputValue = this.retrieveInputValueObject(inputName);
    if (inputValue == null) {
      return null;
    }
    
    if (!(inputValue instanceof Integer)) {
      throw new RuntimeException("Expecting integer but was: " + inputValue.getClass() + " for " + inputName + ": '" + inputValue + "'");
    }
    
    return (Integer)inputValue;
  }

  /**
   * 
   * @param inputName
   * @return the string value of the integer
   */
  public String retrieveInputValueString(String inputName) {
    
    Object inputValue = this.retrieveInputValueObject(inputName);
    if (inputValue == null) {
      return null;
    }
    
    if (!(inputValue instanceof String)) {
      throw new RuntimeException("Expecting string but was: " + inputValue.getClass() + " for " + inputName + ": '" + inputValue + "'");
    }
    
    return (String)inputValue;
  }

  public void setGrouperSession(GrouperSession grouperSession) {
    this.grouperSession = grouperSession;
  }

  /**
   * get out of GSH with return code
   * @param returnCode
   */
  public void gshReturn() {
    gshReturn(0);
  }
  
  /**
   * get out of GSH with return code
   * @param returnCode
   */
  public void gshReturn(int returnCode) {
    throw new GrouperGroovyExit(returnCode);
  }

}
