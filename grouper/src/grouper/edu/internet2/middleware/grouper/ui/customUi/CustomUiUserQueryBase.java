/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public abstract class CustomUiUserQueryBase {

  /**
   * helper for debug map
   * @param key
   * @param value
   */
  public void debugMapPut(String key, Object value) {
    
    Map<String, Object> debugMap = this.customUiEngine == null ? null :  this.customUiEngine.getDebugMap();
    
    if (debugMap == null) {
      return;
    }
    debugMap.put(this.getDebugMapPrefix() + key, value);

  }
  
  /**
   * reference back up to engine
   */
  private CustomUiEngine customUiEngine;
  
  /**
   * reference back up to engine
   * @return the customUiEngine
   */
  public CustomUiEngine getCustomUiEngine() {
    return this.customUiEngine;
  }

  
  /**
   * reference back up to engine
   * @param customUiEngine the customUiEngine to set
   */
  public void setCustomUiEngine(CustomUiEngine customUiEngine) {
    this.customUiEngine = customUiEngine;
  }

  /**
   * name for debugging
   */
  private String debugMapPrefix;
  
  
  
  /**
   * name for debugging
   * @return the name
   */
  public String getDebugMapPrefix() {
    return this.debugMapPrefix;
  }


  
  /**
   * name for debugging
   * @param name1 the nasetDebugMapPrefixet
   */
  public void setDebugMapPrefix(String name1) {
    if (name1 == null) { 
      name1 = GrouperUtil.uniqueId() + "_";
    } else if (!name1.endsWith("_")) {
      name1 += "_";
    }
    this.debugMapPrefix = name1;
  }


  /**
   * 
   */
  public CustomUiUserQueryBase() {
    this.debugMapPrefix = GrouperUtil.uniqueId() + "_";
  }

}
