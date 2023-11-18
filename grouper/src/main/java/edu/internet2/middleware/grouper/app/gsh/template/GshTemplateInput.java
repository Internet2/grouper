package edu.internet2.middleware.grouper.app.gsh.template;


public class GshTemplateInput {
  
  private String name;
  
  private String valueString;
  
  public GshTemplateInput assignName(String name) {
    this.name = name;
    return this;
  }
  
  public GshTemplateInput assignValueString(String valueString) {
    this.valueString = valueString;
    return this;
  }

  public String getName() {
    return name;
  }
  
  public String getValueString() {
    return valueString;
  }

}
