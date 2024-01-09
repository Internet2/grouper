package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.Map;

import edu.internet2.middleware.subject.Subject;

public class GshTemplateDecorateForUiInput {

  /**
   * if this is a new form (requested from url)
   * @return
   */
  public boolean isNewForm() {
    return newForm;
  }

  /**
   * if this is a submit form button press
   * @return
   */
  public boolean isSubmit() {
    return submit;
  }


  /**
   * remove an item to hide it
   */
  private Map<String, GshTemplateInputConfigAndValue> gshTemplateInputConfigAndValues;
  private Subject currentSubject;
  private String templateConfigId;
  private Subject actAsSubject; // only for WS

  /**
   * if this is a new form (requested from url)
   */
  private boolean newForm = false;
  
  /**
   * if this is a submit form button press
   */
  private boolean submit = false;
  
  
  
  /**
   * if this is a new form (requested from url)
   * @param newForm
   */
  public void setNewForm(boolean newForm) {
    this.newForm = newForm;
  }

  /**
   * if this is a submit form button press
   * @param submit
   */
  public void setSubmit(boolean submit) {
    this.submit = submit;
  }


  /**
   * input config id of the event that triggered the call or null if new template
   */
  private String eventConfigId;
  
  /**
   * input config id of the event that triggered the call or null if new template
   * @return
   */
  public String getEventConfigId() {
    return eventConfigId;
  }

  /**
   * input config id of the event that triggered the call or null if new template
   * @param eventConfigId
   */
  public void setEventConfigId(String eventConfigId) {
    this.eventConfigId = eventConfigId;
  }

  /**
   * remove an item to hide it
   * @return
   */
  public Map<String, GshTemplateInputConfigAndValue> getGshTemplateInputConfigAndValues() {
    return gshTemplateInputConfigAndValues;
  }

  /**
   * remove an item to hide it
   * @param gshTemplateInputConfigAndValues
   */
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
