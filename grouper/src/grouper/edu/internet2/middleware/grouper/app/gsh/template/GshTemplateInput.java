package edu.internet2.middleware.grouper.app.gsh.template;


public class GshTemplateInput {
  
  private String name;
  
  private Object value;
  
  public GshTemplateInput assignName(String name) {
    this.name = name;
    return this;
  }
  
  public GshTemplateInput assignValue(Object value) {
    this.value = value;
    return this;
  }

  public String getName() {
    return name;
  }
  
  public Object getValue() {
    return value;
  }

}
