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
 * $Id: HttpContentType.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.util;


/**
 * http content type
 */
public enum HttpContentType {

  /** plain text content type */
  TEXT_PLAIN("text/plain"),
  
  /** xml content type */
  TEXT_XML("text/xml;charset=utf-8"),
  
  /** text html content type */
  TEXT_HTML("text/html"),
  
  /** application json content type */
  APPLICATION_JSON("application/json;charset=utf-8"),
  
  /** text comma separated values */
  TEXT_CSV("text/csv");
  
  /**
   * content type for HTTP
   */
  private String contentType;
  
  /**
   * construct with content type
   * @param theContentType
   */
  private HttpContentType(String theContentType) {
    this.contentType = theContentType;
  }
  
  /**
   * getter for contentType
   * @return contentType
   */
  public String getContentType() {
    return this.contentType;
  }
  
  /**
   * 
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return this.getContentType();
  }
}
