package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateRuntime {
  
  /**
   * input name to value
   */
  private Map<String, Object> inputNameToValue = new HashMap<String, Object>();
  
  /**
   * 
   * @param inputName
   * @param inputValue
   */
  public void assignInputValueInteger(String inputName, Integer inputValue) {
    this.inputNameToValue.put(inputName, inputValue);
  }
  
  /**
   * 
   * @param inputName
   * @param inputValue
   */
  public void assignInputValueBoolean(String inputName, Boolean inputValue) {
    this.inputNameToValue.put(inputName, inputValue);
  }
  
  /**
   * 
   * @param inputName
   * @param inputValue
   */
  public void assignInputValueString(String inputName, String inputValue) {
    this.inputNameToValue.put(inputName, inputValue);
  }
  
  /**
   * 
   * @param inputName
   * @return the integer value of the integer
   */
  public Integer retrieveInputValueInteger(String inputName) {
    
    if (!this.inputNameToValue.containsKey(inputName)) {
      throw new RuntimeException("Cant find input: '" + inputName + "'");
    }
    
    Object inputValue = this.inputNameToValue.get(inputName);
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
   * @return the boolean value of the integer
   */
  public Boolean retrieveInputValueBoolean(String inputName) {
    
    if (!this.inputNameToValue.containsKey(inputName)) {
      throw new RuntimeException("Cant find input: '" + inputName + "'");
    }
    
    Object inputValue = this.inputNameToValue.get(inputName);
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
   * @return the string value of the integer
   */
  public String retrieveInputValueString(String inputName) {
    
    if (!this.inputNameToValue.containsKey(inputName)) {
      throw new RuntimeException("Cant find input: '" + inputName + "'");
    }
    
    Object inputValue = this.inputNameToValue.get(inputName);
    if (inputValue == null) {
      return null;
    }
    
    if (!(inputValue instanceof String)) {
      throw new RuntimeException("Expecting string but was: " + inputValue.getClass() + " for " + inputName + ": '" + inputValue + "'");
    }
    
    return (String)inputValue;
  }
  
  private Subject currentSubject;
  
  private GrouperSession grouperSession;
  
  private static ThreadLocal<GshTemplateRuntime> threadLocalGshTemplateRuntime = new InheritableThreadLocal<GshTemplateRuntime>();

  
  public Subject getCurrentSubject() {
    return currentSubject;
  }

  
  public void setCurrentSubject(Subject currentSubject) {
    this.currentSubject = currentSubject;
  }

  
  public GrouperSession getGrouperSession() {
    return grouperSession;
  }

  
  public void setGrouperSession(GrouperSession grouperSession) {
    this.grouperSession = grouperSession;
  }
  
  public static GshTemplateRuntime retrieveGshTemplateRuntime() {
    return threadLocalGshTemplateRuntime.get();
  }
  
  
  public static void assignThreadLocalGshTemplateRuntime(GshTemplateRuntime gshTemplateRuntime) {
    threadLocalGshTemplateRuntime.set(gshTemplateRuntime);
  }
  
  public static void removeThreadLocalGshTemplateRuntime() {
    threadLocalGshTemplateRuntime.remove();
  }

  /**
   * owner stem name where template was called
   */
  private String ownerStemName;

  /**
   * owner stem name where template was called
   * @param ownerStemName
   */
  public void setOwnerStemName(String ownerStemName) {
    this.ownerStemName = ownerStemName;
  }

  /**
   * owner group name where template was called
   */
  private String ownerGroupName;
  
  /**
   * owner group name where template was called
   * @param ownerGroupName
   */
  public void setOwnerGroupName(String ownerGroupName) {
    this.ownerGroupName = ownerGroupName;
  }

  /**
   * owner stem name where template was called
   * @return
   */
  public String getOwnerStemName() {
    return ownerStemName;
  }

  /**
   * owner group name where template was called
   * @return
   */
  public String getOwnerGroupName() {
    return ownerGroupName;
  }
  
}
