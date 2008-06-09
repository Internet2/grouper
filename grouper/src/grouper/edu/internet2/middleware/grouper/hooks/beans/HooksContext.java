/*
 * @author mchyzer
 * $Id: HooksContext.java,v 1.1.2.1 2008-06-09 05:52:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



/**
 * context in which hooks are running
 */
public class HooksContext {
  
  /** global attributes, threadsafe */
  private static Map<String, Object> attributeGlobal = 
    new HashMap<String, Object>();
  
  /**
   * if this context is asynchronous
   */
  private boolean asynchronous = false;
  
  /**
   * constructor
   */
  public HooksContext() {
    
  }
  
  /**
   * constructor
   * @param theAsynchronous
   * @param synchronousContext 
   */
  public HooksContext(boolean theAsynchronous, HooksContext synchronousContext) {
    this.asynchronous = theAsynchronous;
    if (this.asynchronous) {
      //if its asynchronous, then remove the thread local ones
      threadLocalAttribute().clear();
      
      if (synchronousContext != null) {
        //carry over the attributes, which are threadsafe
        if (synchronousContext.attributeLocal != null) {
          for (String key : synchronousContext.attributeLocal.keySet()) {
            
            HooksAttribute hooksAttribute = synchronousContext.attributeLocal.get(key);
            if (hooksAttribute.isThreadSafe()) {
              threadLocalAttribute().put(key, hooksAttribute);
            }
          }
        }
        //copy over the threadlocal to the local attributes
        for (String key : threadLocalAttribute().keySet()) {
          
          HooksAttribute hooksAttribute = threadLocalAttribute().get(key);
          if (hooksAttribute.isThreadSafe()) {
            threadLocalAttribute().put(key, hooksAttribute);
          }
        }
        
      }
    }
  }
  
  /**
   * set a global attribute
   * @param key
   * @param value
   */
  public static void setAttributeGlobal(String key, Object value) {
    attributeGlobal.put(key, value);
  }
  
  /**
   * get the context in which the hooks are running
   * @return the context
   */
  public GrouperContextType getGrouperContextType() {
    return GrouperBuiltinContextType.currentGrouperContext();
  }

  /**
   * thread local hooks attribute, access from: threadLocalAttribute()
   */
  private static ThreadLocal<Map<String, HooksAttribute>> threadLocalAttribute = new ThreadLocal<Map<String, HooksAttribute>>();

  /**
   * lazy load the threadlocal attribute
   * @return the attribute map
   */
  private static Map<String, HooksAttribute> threadLocalAttribute() {
    Map<String, HooksAttribute> theMap = threadLocalAttribute.get();
    if (theMap == null) {
      theMap = new HashMap<String, HooksAttribute>();
      threadLocalAttribute.set(theMap);
    }
    return theMap;
    
  }
  
  /**
   * set a threadlocal attribute
   * @param key
   * @param value
   * @param threadSafe if this should be set for hooks spawned in new thread
   */
  public static void setAttributeThreadLocal(String key, Object value, boolean threadSafe) {
    if (value == null) {
      threadLocalAttribute().remove(key);
    } else {
      threadLocalAttribute().put(key, new HooksAttribute(threadSafe, value));
    }
  }
  
  /**
   * local attributes just for this context
   */
  private Map<String, HooksAttribute> attributeLocal = new HashMap<String, HooksAttribute>();
  
  /**
   * keys of attributes (all put together, global, threadlocal, local
   * @return the key
   */
  public Set<String> attributeKeySet() {
    Set<String> keySet = new HashSet<String>();
    keySet.addAll(attributeGlobal.keySet());
    //if not asynchronous, then use threadlocals, else copied into local
    if (!this.asynchronous) {
      keySet.addAll(threadLocalAttribute().keySet());
    }
    keySet.addAll(attributeLocal.keySet());
    return keySet;
  }
  
  /**
   * get an attribute 
   * @param key
   * @return the object or null if not found
   */
  public Object getAttribute(String key) {
    HooksAttribute hooksAttribute = attributeLocal.get(key);
    Object value = null;
    
    //dont check thread local if asynchronous 
    if (hooksAttribute == null && !this.asynchronous) {
      hooksAttribute = threadLocalAttribute().get(key);
    }
    if (hooksAttribute == null) {
      value = attributeGlobal.get(key);
      if (value != null) {
        return value;
      }
    }
    if (hooksAttribute != null) {
      return hooksAttribute.getValue();
    }
    return null;
  }
  
}
