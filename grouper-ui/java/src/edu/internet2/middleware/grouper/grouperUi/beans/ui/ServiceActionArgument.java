package edu.internet2.middleware.grouper.grouperUi.beans.ui;

public class ServiceActionArgument {
  
  private String name;
  
  private String value;
  
  
  public ServiceActionArgument() {}

  public ServiceActionArgument(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
  
  

}
