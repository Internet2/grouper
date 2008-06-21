package edu.internet2.middleware.grouper.hooks.beans;

/**
 * type of context
 */
public enum GrouperBuiltinContextType implements GrouperContextType {
  
  /**
   * normal API call
   */
  GROUPER_API,
  
  /**
   * from the UI
   */
  GROUPER_UI,
  
  /**
   * from the ws
   */
  GROUPER_WS,
  
  /**
   * from gruoper shell
   */
  GSH,
  
  /**
   * from undeleted subject utility
   */
  USDU,
  
  /**
   * from grouper loader
   */
  GROUPER_LOADER;
  
  /**
   * global default context (if not one set in thread local)
   */
  private static GrouperContextType defaultContext = null;

  /**
   * global default context (if not one set in thread local)
   * @param defaultContext the defaultContext to set
   */
  public static void setDefaultContext(GrouperContextType defaultContext) {
    GrouperBuiltinContextType.defaultContext = defaultContext;
  }
 
  /**
   * thread local grouper context type overrides the global one
   */
  private static ThreadLocal<GrouperContextType> 
    threadLocalGrouperContextType = new ThreadLocal<GrouperContextType>();
  
  /**
   * 
   * @param grouperContextType
   */
  public static void setThreadLocalContext(GrouperContextType grouperContextType) {
    if (grouperContextType == null) {
      threadLocalGrouperContextType.remove();
    } else {
      threadLocalGrouperContextType.set(grouperContextType);
    }
  }
  
  /**
   * current grouper context
   * @return the threadlocal if there is one there, or global 
   */
  public static GrouperContextType currentGrouperContext() {
    GrouperContextType grouperContextType = threadLocalGrouperContextType.get();
    
    if (grouperContextType == null) {
      grouperContextType = defaultContext;
    }
    
    return grouperContextType;
  }
  
}