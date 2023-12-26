package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.Map;

public abstract class GshTemplateV2 {

  public abstract void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output);
  
  /**
   * how many lines are prepended to the script
   */
  private int scriptPrependHeaders = 0;
  
  /**
   * how many lines are prepended to the script
   * @return
   */
  public int getScriptPrependHeaders() {
    return scriptPrependHeaders;
  }
  /**
   * lightweight
   */
  private boolean lightWeight;

  /**
   * lightweight
   * @return
   */
  public boolean isLightWeight() {
    return lightWeight;
  }
  
  /**
   * lightweight
   * @param lightWeight
   */
  public void setLightWeight(boolean lightWeight) {
    this.lightWeight = lightWeight;
  }

  /**
   * how many lines are prepended to the script
   * @param scriptPrependHeaders
   */
  public void setScriptPrependHeaders(int scriptPrependHeaders) {
    this.scriptPrependHeaders = scriptPrependHeaders;
  }

  /**
   * source for this template for error lines
   */
  private String source;

  /**
   * source for this template for error lines
   * @return
   */
  public String getSource() {
    return source;
  }

  /**
   * source for this template for error lines
   * @param source
   */
  public void setSource(String source) {
    this.source = source;
  }

  public void copyStateFrom(GshTemplateV2 gshTemplateV2) {
    this.setLightWeight(gshTemplateV2.isLightWeight());
    this.setScriptPrependHeaders(gshTemplateV2.getScriptPrependHeaders());
    this.setSource(gshTemplateV2.getSource());
  }

  public void decorateTemplateForUiDisplay(GshTemplateDecorateForUiInput gshTemplateDecorateForUiInput) {
    
  }
  
  
}
