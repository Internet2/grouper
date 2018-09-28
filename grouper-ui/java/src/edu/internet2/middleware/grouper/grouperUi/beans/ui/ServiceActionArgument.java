package edu.internet2.middleware.grouper.grouperUi.beans.ui;

public class ServiceActionArgument {
  
  /**
   * name of the service action argument
   */
  private String name;
  
  /**
   * value of the service action argument
   */
  private String value;
  
  
  public ServiceActionArgument(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * name of the service action argument
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * value of the service action argument
   * @return
   */
  public String getValue() {
    return value;
  }


}
