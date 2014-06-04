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
 * $Id: XstreamPocMember.java,v 1.3 2009-04-13 20:24:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.poc;


/**
 * poc class for members
 */
public class XstreamPocMember {

  /**
   * 
   * @param theName
   * @param theDescription
   */
  public XstreamPocMember(String theName, String theDescription) {
    this.name = theName;
    this.description = theDescription;
  }
  
  /**
   * 
   */
  public XstreamPocMember() {
    //empty
  }
  
  /** */
  private String name;
  
  /** */
  private String description;

  /**
   * 
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * 
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * 
   * @return description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * 
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }
  
}
