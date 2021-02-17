package edu.internet2.middleware.grouper.app.gsh.template;


public class GshValidationLine {

  private String inputName;
  
  private String text;
  
  public GshValidationLine(String text) {
    this.text = text;
  }
  
  public GshValidationLine(String inputName, String text) {
    this.inputName = inputName;
    this.text = text;
  }
    
  public String getInputName() {
    return inputName;
  }

  public String getText() {
    return text;
  }
  
  
}
