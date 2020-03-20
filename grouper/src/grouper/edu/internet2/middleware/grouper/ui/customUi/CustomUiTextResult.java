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
