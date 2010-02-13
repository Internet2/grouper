/*
 * @author mchyzer
 * $Id: GuiSubject.java,v 1.2 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.subjectPicker;

import java.io.Serializable;


/**
 * subject for subject picker result
 */
public class PickerResultJavascriptSubject implements Serializable {
  
  /** subject */
  private SubjectPickerJavascriptBean subject;

  /**
   * index on page
   */
  private int index = 0;

  /**
   * @return the index
   */
  public int getIndex() {
    return this.index;
  }

  /**
   * @param index1 the index to set
   */
  public void setIndex(int index1) {
    this.index = index1;
  }

  /**
   * construct with subject
   * @param subject1
   */
  public PickerResultJavascriptSubject(SubjectPickerJavascriptBean subject1) {
    this.subject = subject1;
    this.screenLabel = null;
  }

  /**
   * get screen label
   * @return screen label
   */
  public String getScreenLabel() {
    return this.screenLabel;
  }

  /** cache this */
  private String screenLabel;

  /**
   * get subject id for  caller
   * @return subject id
   */
  public String getSubjectId() {
    
    String subjectId = this.subject.getId();
    return subjectId;
  }

  /**
   * subject
   * @return the subject
   */
  public SubjectPickerJavascriptBean getSubject() {
    return this.subject;
  }
  
}
