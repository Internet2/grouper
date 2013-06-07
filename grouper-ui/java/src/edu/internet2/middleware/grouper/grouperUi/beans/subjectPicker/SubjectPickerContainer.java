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
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.subjectPicker;

import java.io.Serializable;
import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.grouperUi.serviceLogic.SubjectPicker;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * bean for subject picker.  holds all state for this module
 */
public class SubjectPickerContainer implements Serializable {

  /**
   * check config file or defaults
   * @param key
   * @return the value
   */
  public String configValue(String key) {
    return configValue(key, true);
  }

  /**
   * check config file or defaults
   * @param key
   * @param exceptionIfNotThere
   * @return the value
   */
  public String configValue(String key, boolean exceptionIfNotThere) {
    String subjectPickerName = this.getSubjectPickerName();
    //lets see if this config file has a value
    String value = null;
    try {
      value = SubjectPicker.configFileValue(subjectPickerName, key);
    } catch (SubjectPickerConfigNotFoundException spcnfe) {
      //try the default
      String mediaPropertiesKey = "subjectPicker.defaultSettings." + key;
      try {
        value = GrouperUiConfig.retrieveConfig().propertyValueString(mediaPropertiesKey);
      } catch (RuntimeException mre) {
        if (exceptionIfNotThere) {
          throw new  RuntimeException("cant find config for key '" + key + "' in subjectPicker config"
              + " (or default in grouper-ui.properties: " + mediaPropertiesKey + "), and subjectPickerName: " 
              + subjectPickerName + ".\n" + ExceptionUtils.getFullStackTrace(spcnfe) , mre);
        }
      }
    }
    return value;
  }

  /**
   * check config file or defaults
   * @param key 
   * @return true if true, false if false
   */
  public boolean configValueBoolean(
      String key) {
    
    String valueString = configValue(key);
    
    if (StringUtils.equalsIgnoreCase(valueString, "true") || StringUtils.equalsIgnoreCase(valueString, "t")) {
      return true;
    }
    
    if (StringUtils.equalsIgnoreCase(valueString, "false") || StringUtils.equalsIgnoreCase(valueString, "f")) {
      return false;
    }
    //throw descriptive exception
    throw new RuntimeException("Invalid value: '" + valueString + "' for key '" + key + "' in subjectPicker config" +
        " (or default).  Should be true or false: '" + this.getSubjectPickerName() + "'");
  }

  /**
   * based on request get a media int
   * @param key 
   * @return true if true, false if false
   */
  public int configValueInt(
      String key) {
    
    String valueString = configValue(key);
    
    try {
      return GrouperUtil.intValue(valueString);
    } catch (Exception e) {
      //throw descriptive exception
      throw new RuntimeException("Invalid value: '" + valueString + "' for key '" + key + "' in subjectPicker config" +
          " (or default).  Should be an int", e);
    }
  }

  /** script of all the subject objects */
  private String subjectsScript;
  
  /**
   * script of all the subject objects
   * @return the subjectsScript
   */
  public String getSubjectsScript() {
    return this.subjectsScript;
  }
  
  /**
   * script of all the subject objects
   * @param subjectsScript1 the subjectsScript to set
   */
  public void setSubjectsScript(String subjectsScript1) {
    this.subjectsScript = subjectsScript1;
  }

  /**
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("subjectPickerContainer", this);
  }

  /**
   * retrieveFromRequest, cannot be null
   * @return the app state in request scope
   */
  public static SubjectPickerContainer retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    SubjectPickerContainer subjectPickerContainer = (SubjectPickerContainer)httpServletRequest
      .getAttribute("subjectPickerContainer");
    if (subjectPickerContainer == null) {
      subjectPickerContainer = new SubjectPickerContainer();
      subjectPickerContainer.storeToRequest();
    }
    return subjectPickerContainer;
  }

  /**
   * members in result
   */
  private PickerResultSubject[] pickerResultSubjects;
  
  /**
   * string the user is searching for
   */
  private String searchString;
  
  /**
   * if there is an error in the search (e.g. too many results
   */
  private boolean hasError;
  
  /**
   * error message for screen
   */
  private String errorMessage;

  
  /**
   * members in result
   * @return the PickerResultSubjects
   */
  public PickerResultSubject[] getPickerResultSubjects() {
    return this.pickerResultSubjects;
  }

  
  /**
   * members in result
   * @param thePickerResultSubjects the guiSubjects to set
   */
  public void setPickerResultSubjects(PickerResultSubject[] thePickerResultSubjects) {
    this.pickerResultSubjects = thePickerResultSubjects;
  }

  
  /**
   * string the user is searching for
   * @return the searchString
   */
  public String getSearchString() {
    return this.searchString;
  }

  
  /**
   * string the user is searching for
   * @param searchString1 the searchString to set
   */
  public void setSearchString(String searchString1) {
    this.searchString = searchString1;
  }

  /**
   * if we are submitting to a URL instead of using opener
   * @return the url
   */
  public boolean isSubmitToUrl() {
    return !StringUtils.isBlank(this.getSubmitResultToUrl());
  }

  /**
   * the url to submit to or blank to use opener (same domain)
   * @return the url to submit to or blank to use opener (same domain)
   */
  public String getSubmitResultToUrl() {
    return this.configValue("submitResultToUrl");
  }
  
  /**
   * if there is an error in the search (e.g. too many results
   * @return the hasError
   */
  public boolean isHasError() {
    return this.hasError;
  }

  
  /**
   * if there is an error in the search (e.g. too many results
   * @param hasError1 the hasError to set
   */
  public void setHasError(boolean hasError1) {
    this.hasError = hasError1;
  }

  
  /**
   * error message for screen
   * @return the errorMessage
   */
  public String getErrorMessage() {
    return this.errorMessage;
  }

  
  /**
   * error message for screen
   * @param errorMessage1 the errorMessage to set
   */
  public void setErrorMessage(String errorMessage1) {
    this.errorMessage = errorMessage1;
  }
  
  /**
   * 
   * @return the subject picker name
   */
  public String getSubjectPickerName() {
    HttpServletRequest request = GrouperUiFilter.retrieveHttpServletRequest();
    String subjectPickerName = request.getParameter("subjectPickerName");
    if (StringUtils.isBlank(subjectPickerName)) {
      throw new RuntimeException("Need to pass in subjectPickerName in URL");
    }
    if (!subjectPickerName.matches("^[a-zA-Z0-9_]+$")) {
      throw new RuntimeException("Invalid subject picker name, but be alpha numeric or underscore: " + subjectPickerName);
    }
    return subjectPickerName;
  }
  
  /**
   * 
   * @return the subject picker name
   */
  public String getSubjectPickerElementName() {
    HttpServletRequest request = GrouperUiFilter.retrieveHttpServletRequest();
    String subjectPickerElementName = request.getParameter("subjectPickerElementName");
    if (StringUtils.isBlank(subjectPickerElementName)) {
      throw new RuntimeException("Need to pass in subjectPickerElementName in URL");
    }
    if (!subjectPickerElementName.matches("^[a-zA-Z0-9_]+$")) {
      throw new RuntimeException("Invalid subject picker element name, but be alpha numeric or underscore");
    }
    return subjectPickerElementName;
  }

  /**
   * cancel text
   * @return cancel text
   */
  public String getCancelText() {
    return this.textMessage("cancelText");
  }

  
  /**
   * main title of screen
   * @return title
   */
  public String getHeader() {
    return this.textMessage("header");
  }

  /**
   * get the text or default
   * @param key
   * @return the text or default
   */
  public String textMessage(String key) {
    
    String localKey = "subjectPicker." + this.getSubjectPickerName() + "." + key;
    String defaultKey = "subjectPickerDefault." + key;
    try {
      return GrouperUiUtils.message(localKey);
    } catch (MissingResourceException mre) {
      try {
        return GrouperUiUtils.message(defaultKey);
      } catch (MissingResourceException mre2) {
        throw new RuntimeException("Cant find text in nav.properties " +
        		"for subjectPicker local: " + localKey + ", or in default: " + defaultKey 
        		+ ", " + ExceptionUtils.getFullStackTrace(mre), mre2);
      }
    }
  }
  
  /**
   * searchSectionTitle
   * @return title
   */
  public String getSearchSectionTitle() {
    return this.textMessage("searchSectionTitle");
  }

  /**
   * resultsSectionTitle
   * @return title
   */
  public String getResultsSectionTitle() {
    return this.textMessage("resultsSectionTitle");
  }

  /**
   * search button text
   * @return search button text
   */
  public String getSearchButtonText() {
    return this.textMessage("searchButtonText");
  }
}
