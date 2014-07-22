/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.hooks.beans;

/**
 * type of context
 */
public enum GrouperContextTypeBuiltIn implements GrouperContextType {
  
  /**
   * normal API call
   */
  GROUPER_API,
  
  /**
   * ant tools like schema-export etc
   */
  ANT_TOOLS,
  
  /**
   * from the UI
   */
  GROUPER_UI,
  
  /**
   * from the ws
   */
  GROUPER_WS,
  
  /**
   * from grouper shell
   */
  GSH,
  
  /**
   * if the context type if not known
   */
  UNKNOWN,
  
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
    GrouperContextTypeBuiltIn.defaultContext = defaultContext;
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
   * current grouper context.  Will not return null, if null will return
   * UNKNOWN.  This defaults to the default global context, but if the
   * threadlocal one was set, use that instead.
   * 
   * @return the threadlocal if there is one there, or global 
   */
  public static GrouperContextType currentGrouperContext() {
    GrouperContextType grouperContextType = threadLocalGrouperContextType.get();
    
    if (grouperContextType == null) {
      grouperContextType = defaultContext;
    }
    
    if (grouperContextType == null) {
      return UNKNOWN;
    }
    
    return grouperContextType;
  }

  
  /**
   * @return the threadLocalGrouperContextType
   */
  public static GrouperContextType _internal_getThreadLocalGrouperContextType() {
    return threadLocalGrouperContextType.get();
  }
  
}
