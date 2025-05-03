package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.util.GrouperUtil;

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

  public GshTemplateInput assignValueString(String value) {
    this.value = value;
    return this;
  }

  public String getName() {
    return name;
  }
  
  public Object getValue() {
    return value;
  }

  /**
   * @deprecated you should use getValue() and handle the file if necessary
   */
  @Deprecated
  public String getValueString() {
    return GrouperUtil.stringValue(value);
  }


}
