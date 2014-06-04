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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.subjectPicker;

import java.util.Map;


/**
 *
 */
public class SubjectPickerJavascriptBean {

  /** */
  private Map<String, String[]> attributes = null;
  /** */
  private String description;
  /** */
  private String id;
  /** */
  private String name;
  
  /**
   * @param attributes1
   * @param description1
   * @param id1
   * @param name1
   * @param sourceId1
   * @param typeName1
   */
  public SubjectPickerJavascriptBean(Map<String, String[]> attributes1,
      String description1, String id1, String name1, String sourceId1, String typeName1) {
    super();
    this.attributes = attributes1;
    this.description = description1;
    this.id = id1;
    this.name = name1;
    this.sourceId = sourceId1;
    this.typeName = typeName1;
  }

  /** sourceId */
  private String sourceId;
  
  /** */
  private String typeName;
  
  /**
   * @return the attributes
   */
  public Map<String, String[]> getAttributes() {
    return this.attributes;
  }
  
  /**
   * @param attributes1 the attributes to set
   */
  public void setAttributes(Map<String, String[]> attributes1) {
    this.attributes = attributes1;
  }
  
  /**
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }
  
  /**
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }
  
  /**
   * @return the id
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }
  
  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }
  
  /**
   * @return the sourceId
   */
  public String getSourceId() {
    return this.sourceId;
  }
  
  /**
   * @param sourceId1 the sourceId to set
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }
  
  /**
   * @return the typeName
   */
  public String getTypeName() {
    return this.typeName;
  }
  
  /**
   * @param typeName1 the typeName to set
   */
  public void setTypeName(String typeName1) {
    this.typeName = typeName1;
  }

}
