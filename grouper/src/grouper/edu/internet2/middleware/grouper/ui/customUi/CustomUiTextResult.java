/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;


/**
 *
 */
public class CustomUiTextResult {

  /**
   * 
   */
  public CustomUiTextResult() {
  }

    
  private String endIfMatches;
  
  

  
  /**
   * @return the endIfMatches
   */
  public String getEndIfMatches() {
    return this.endIfMatches;
  }



  
  /**
   * @param endIfMatches the endIfMatches to set
   */
  public void setEndIfMatches(String endIfMatches) {
    this.endIfMatches = endIfMatches;
  }

  private String theDefault;
  
  
  
  
  /**
   * @return the theDefault
   */
  public String getTheDefault() {
    return this.theDefault;
  }


  
  /**
   * @param theDefault the theDefault to set
   */
  public void setTheDefault(String theDefault) {
    this.theDefault = theDefault;
  }

  /**
   * 
   */
  private String scriptResult;
  
  
  /**
   * @return the shouldDisplay
   */
  public String getScriptResult() {
    return this.scriptResult;
  }

  
  /**
   * @param scriptResult the shouldDisplay to set
   */
  public void setScriptResult(String scriptResult) {
    this.scriptResult = scriptResult;
  }

  /**
   * type to check
   */
  private CustomUiTextType customUiTextType;
  
  /**
   * @return the customUiTextType
   */
  public CustomUiTextType getCustomUiTextType() {
    return this.customUiTextType;
  }
  
  /**
   * @param customUiTextType1 the customUiTextType to set
   */
  public void setCustomUiTextType(CustomUiTextType customUiTextType1) {
    this.customUiTextType = customUiTextType1;
  }
  
  private CustomUiTextConfigBean customUiTextConfigBean;
  
  /**
   * @return the customUiTextConfigBean
   */
  public CustomUiTextConfigBean getCustomUiTextConfigBean() {
    return this.customUiTextConfigBean;
  }
  
  /**
   * @param customUiTextConfigBean1 the customUiTextConfigBean to set
   */
  public void setCustomUiTextConfigBean(CustomUiTextConfigBean customUiTextConfigBean1) {
    this.customUiTextConfigBean = customUiTextConfigBean1;
  }
  
  private String textResult;

  
  /**
   * @return the textResult
   */
  public String getTextResult() {
    return this.textResult;
  }

  
  /**
   * @param textResult the textResult to set
   */
  public void setTextResult(String textResult) {
    this.textResult = textResult;
  }
  
  
}
