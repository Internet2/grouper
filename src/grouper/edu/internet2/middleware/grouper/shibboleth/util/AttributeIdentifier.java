/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth.util;

/**
 * Defines a subject attribute id and source.
 */
public class AttributeIdentifier {

  /** the attribute id */
  private String id;

  /** the attribute's source */
  private String source;

  /**
   * Get the attribute id.
   * 
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Set the attribute id.
   * 
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the source id.
   * 
   * @return source id
   */
  public String getSource() {
    return source;
  }

  /**
   * Set the source id.
   * 
   * @param source
   * 
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "id '" + id + "' source '" + source + "'";
  }

}
