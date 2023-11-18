/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
