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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.serviceLogic.SubjectPicker;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.SubjectPicker.SubjectPickerSourceProperties;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * subject for subject picker result
 */
public class PickerResultSubject implements Serializable, Comparable<PickerResultSubject> {
  
  /** see if the subject matches the id or identifier */
  private boolean matchesSubjectIdOrIdentifier = false;
  
  /**
   * 
   * @return true if matches
   */
  public boolean isMatchesSubjectIdOrIdentifier() {
    return this.matchesSubjectIdOrIdentifier;
  }


  /**
   * 
   * @param matchesSubjectIdOrIdentifier1
   */
  public void setMatchesSubjectIdOrIdentifier(boolean matchesSubjectIdOrIdentifier1) {
    this.matchesSubjectIdOrIdentifier = matchesSubjectIdOrIdentifier1;
  }

  /** subject */
  private Subject subject;

  /** picker result javascript subject */
  private PickerResultJavascriptSubject pickerResultJavascriptSubject;
  
  
  /**
   * @return the pickerResultJavascriptSubject
   */
  public PickerResultJavascriptSubject getPickerResultJavascriptSubject() {
    return this.pickerResultJavascriptSubject;
  }

  
  /**
   * @param pickerResultJavascriptSubject1 the pickerResultJavascriptSubject to set
   */
  public void setPickerResultJavascriptSubject(
      PickerResultJavascriptSubject pickerResultJavascriptSubject1) {
    this.pickerResultJavascriptSubject = pickerResultJavascriptSubject1;
  }

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
   * this is either a variable name or null
   */
  private String subjectObjectName;
  
  
  /**
   * @return the subjectObjectName
   */
  public String getSubjectObjectName() {
    return this.subjectObjectName;
  }

  
  /**
   * @param subjectObjectName1 the subjectObjectName to set
   */
  public void setSubjectObjectName(String subjectObjectName1) {
    this.subjectObjectName = subjectObjectName1;
  }

  /**
   * construct with subject
   * @param subject1
   */
  public PickerResultSubject(Subject subject1) {
    this.subject = subject1;
    this.screenLabel = null;
  }

  /**
   * get screen label
   * @return screen label
   */
  public String getScreenLabel() {
    if (this.screenLabel == null) {
      
      SubjectPickerSourceProperties subjectPickerSourceProperties = SubjectPicker
        .subjectPickerSourceProperties(this.getSubject().getSourceId());
      
      if (subjectPickerSourceProperties == null || StringUtils.isBlank(subjectPickerSourceProperties.getSubjectElForSource())) {
        this.screenLabel = GrouperUiUtils.convertSubjectToLabelConfigured(this.subject);
      } else {
        String subjectElForSource = subjectPickerSourceProperties.getSubjectElForSource();
        //run the screen EL
        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("subject", this.subject);
        variableMap.put("pickerResultSubject", this);
        variableMap.put("grouperUiUtils", new GrouperUiUtils());
        this.screenLabel = GrouperUtil.substituteExpressionLanguage(subjectElForSource, variableMap);
      }
      
      //make sure there is something there
      if (StringUtils.isBlank(this.screenLabel) || StringUtils.equals("null", this.screenLabel)) {
        this.screenLabel = GrouperUiUtils.convertSubjectToLabel(this.subject);
      }
      
    }
    return this.screenLabel;
  }

  /** cache this */
  private String screenLabel;

  /** attributes in string - string format */
  private Map<String, String> attributes = null;

  /**
   * get subject id for  caller
   * @return subject id
   */
  public String getSubjectId() {
    
    String subjectId = this.subject.getId();
    return subjectId;
  }

  /**
   * get subject id for  caller
   * @return subject id
   */
  public String getSourceId() {
    
    String sourceId = this.subject.getSourceId();
    return sourceId;
  }

  /**
   * get subject id for  caller
   * @return subject id
   */
  public String getName() {
    
    String name = this.subject.getName();
    return name;
  }

  /**
   * subject
   * @return the subject
   */
  public Subject getSubject() {
    return this.subject;
  }
  
  /**
   * Gets a map attribute names and value. The map's key
   * contains the attribute name and the map's value
   * contains a Set of attribute value(s).  Note, this only does single valued attributes
   * @return the map of attributes
   */
  @SuppressWarnings({ "cast", "unchecked" })
  public Map<String, String> getAttributes() {
    if (this.attributes == null) {
      Map<String, String> result = new LinkedHashMap<String, String>();
      for (String key : (Set<String>)(Object)GrouperUtil.nonNull(this.subject.getAttributes().keySet())) {
        Object value = this.subject.getAttributes().get(key);
        if (value instanceof String) {
          //if a string
          result.put(key, (String)value);
        } else if (value instanceof Set) {
          //if set of one string, then add it
          if (((Set)value).size() == 1) {
            result.put(key, (String)((Set)value).iterator().next());
          } else if (((Set)value).size() > 1) {
            //put commas in between?  not sure what else to do here
            result.put(key, GrouperUtil.setToString((Set)value));
          }
        }
      }
      this.attributes = result;
    }
    return this.attributes;
  }

  /**
   * 
   * @param subject
   * @param attrName
   * @return the value
   */
  public static String attributeValue(Subject subject, String attrName) {
    if (StringUtils.equalsIgnoreCase("screenLabel", attrName)) {
      return GrouperUiUtils.convertSubjectToLabel(subject);
    }
    if (StringUtils.equalsIgnoreCase("subjectId", attrName)) {
      return subject.getId();
    }
    if (StringUtils.equalsIgnoreCase("name", attrName)) {
      return subject.getName();
    }
    if (StringUtils.equalsIgnoreCase("description", attrName)) {
      return subject.getDescription();
    }
    if (StringUtils.equalsIgnoreCase("typeName", attrName)) {
      return subject.getType().getName();
    }
    if (StringUtils.equalsIgnoreCase("sourceId", attrName)) {
      return subject.getSource().getId();
    }
    if (StringUtils.equalsIgnoreCase("sourceName", attrName)) {
      return subject.getSource().getName();
    }
    //TODO switch this to attribute values comma separated
    return subject.getAttributeValue(attrName);
  }


  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(PickerResultSubject otherPickerResultSubject) {
    
    //move matches to the front
    if (this.matchesSubjectIdOrIdentifier) {
      if (!otherPickerResultSubject.matchesSubjectIdOrIdentifier) {
        return -1;
      }
    }

    //move matches to the front
    if (!this.matchesSubjectIdOrIdentifier) {
      if (otherPickerResultSubject.matchesSubjectIdOrIdentifier) {
        return 1;
      }
    }
    
    String theScreenLabel = StringUtils.defaultString(this.getScreenLabel());
    String otherScreenLabel = StringUtils.defaultString(otherPickerResultSubject.getScreenLabel());
    return theScreenLabel.compareTo(otherScreenLabel);
  }


}
