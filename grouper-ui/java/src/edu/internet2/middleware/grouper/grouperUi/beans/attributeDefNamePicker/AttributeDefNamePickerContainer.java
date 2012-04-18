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
package edu.internet2.middleware.grouper.grouperUi.beans.attributeDefNamePicker;

import java.io.Serializable;
import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.grouperUi.serviceLogic.AttributeDefNamePicker;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * bean for attributeDefName picker.  holds all state for this module
 */
public class AttributeDefNamePickerContainer implements Serializable {

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
    String attributeDefNamePickerName = this.getAttributeDefNamePickerName();
    //lets see if this config file has a value
    String value = null;
    try {
      value = AttributeDefNamePicker.configFileValue(attributeDefNamePickerName, key);
    } catch (AttributeDefNamePickerConfigNotFoundException spcnfe) {
      //try the default
      String mediaPropertiesKey = "attributeDefNamePicker.defaultSettings." + key;
      try {
        value = TagUtils.mediaResourceString(mediaPropertiesKey);
      } catch (MissingResourceException mre) {
        if (exceptionIfNotThere) {
          throw new  RuntimeException("cant find config for key '" + key + "' in attributeDefNamePicker config"
              + " (or default in media.properties: " + mediaPropertiesKey + "), and attributeDefNamePickerName: " 
              + attributeDefNamePickerName + ".\n" + ExceptionUtils.getFullStackTrace(spcnfe) , mre);
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
    throw new RuntimeException("Invalid value: '" + valueString + "' for key '" + key + "' in attributeDefNamePicker config" +
        " (or default).  Should be true or false: '" + this.getAttributeDefNamePickerName() + "'");
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
      throw new RuntimeException("Invalid value: '" + valueString + "' for key '" + key + "' in attributeDefNamePicker config" +
          " (or default).  Should be an int", e);
    }
  }

  /**
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("attributeDefNamePickerContainer", this);
  }

  /**
   * retrieveFromRequest, cannot be null
   * @return the app state in request scope
   */
  public static AttributeDefNamePickerContainer retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    AttributeDefNamePickerContainer attributeDefNamePickerContainer = (AttributeDefNamePickerContainer)httpServletRequest
      .getAttribute("attributeDefNamePickerContainer");
    if (attributeDefNamePickerContainer == null) {
      attributeDefNamePickerContainer = new AttributeDefNamePickerContainer();
      attributeDefNamePickerContainer.storeToRequest();
    }
    return attributeDefNamePickerContainer;
  }

  /**
   * members in result
   */
  private PickerResultAttributeDefName[] pickerResultAttributeDefNames;
  
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
   * @return the PickerResultAttributeDefNames
   */
  public PickerResultAttributeDefName[] getPickerResultAttributeDefNames() {
    return this.pickerResultAttributeDefNames;
  }

  
  /**
   * members in result
   * @param thePickerResultAttributeDefNames the guiAttributeDefNames to set
   */
  public void setPickerResultAttributeDefNames(PickerResultAttributeDefName[] thePickerResultAttributeDefNames) {
    this.pickerResultAttributeDefNames = thePickerResultAttributeDefNames;
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
   * @return the attributeDefName picker name
   */
  public String getAttributeDefNamePickerName() {
    HttpServletRequest request = GrouperUiFilter.retrieveHttpServletRequest();
    String attributeDefNamePickerName = request.getParameter("attributeDefNamePickerName");
    if (StringUtils.isBlank(attributeDefNamePickerName)) {
      throw new RuntimeException("Need to pass in attributeDefNamePickerName in URL");
    }
    if (!attributeDefNamePickerName.matches("^[a-zA-Z0-9_]+$")) {
      throw new RuntimeException("Invalid attributeDefName picker name, but be alpha numeric or underscore: " + attributeDefNamePickerName);
    }
    return attributeDefNamePickerName;
  }
  
  /**
   * 
   * @return the attributeDefName picker name
   */
  public String getAttributeDefNamePickerElementName() {
    HttpServletRequest request = GrouperUiFilter.retrieveHttpServletRequest();
    String attributeDefNamePickerElementName = request.getParameter("attributeDefNamePickerElementName");
    if (StringUtils.isBlank(attributeDefNamePickerElementName)) {
      throw new RuntimeException("Need to pass in attributeDefNamePickerElementName in URL");
    }
    if (!attributeDefNamePickerElementName.matches("^[a-zA-Z0-9_]+$")) {
      throw new RuntimeException("Invalid attributeDefName picker element name, but be alpha numeric or underscore");
    }
    return attributeDefNamePickerElementName;
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
    
    String localKey = "attributeDefNamePicker." + this.getAttributeDefNamePickerName() + "." + key;
    String defaultKey = "attributeDefNamePickerDefault." + key;
    try {
      return GrouperUiUtils.message(localKey);
    } catch (MissingResourceException mre) {
      try {
        return GrouperUiUtils.message(defaultKey);
      } catch (MissingResourceException mre2) {
        throw new RuntimeException("Cant find text in nav.properties " +
        		"for attributeDefNamePicker local: " + localKey + ", or in default: " + defaultKey 
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
