package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.Map;

import edu.internet2.middleware.subject.Subject;

public class GshTemplateDecorateForUiInput {

  private Map<String, GshTemplateInputConfigAndValue> gshTemplateInputConfigAndValues;
  private Subject currentSubject;
  private String templateConfigId;
  private Subject actAsSubject; // only for WS

  
  public Map<String, GshTemplateInputConfigAndValue> getGshTemplateInputConfigAndValues() {
    return gshTemplateInputConfigAndValues;
  }

  
  public void setGshTemplateInputConfigAndValues(
      Map<String, GshTemplateInputConfigAndValue> gshTemplateInputConfigAndValues) {
    this.gshTemplateInputConfigAndValues = gshTemplateInputConfigAndValues;
  }


  public Subject getCurrentSubject() {
    return currentSubject;
  }


  public String getTemplateConfigId() {
    return templateConfigId;
  }


  public void setCurrentSubject(Subject currentSubject) {
    this.currentSubject = currentSubject;
  }


  public void setTemplateConfigId(String templateConfigId) {
    this.templateConfigId = templateConfigId;
  }


  public void setActAsSubject(Subject actAsSubject) {
    this.actAsSubject = actAsSubject;
  }


  public Subject getActAsSubject() {
    return actAsSubject;
  }
  
  
}
