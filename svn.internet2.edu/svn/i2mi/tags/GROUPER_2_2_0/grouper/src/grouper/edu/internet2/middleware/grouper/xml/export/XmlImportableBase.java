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
package edu.internet2.middleware.grouper.xml.export;


/**
 * Hibernated object which can be imported into 
 * @param <T> is the type of the object
 */
public interface XmlImportableBase<T> {

  /**
   * convert to string for log
   * @return the string value for log
   */
  public String xmlToString();
  
  /**
   * see if the update cols are different (e.g. last updated)
   * @param other the one to compare with
   * @return true if so
   */
  public boolean xmlDifferentUpdateProperties(T other);
  
  /**
   * see if the non update cols are different (e.g. name)
   * @param other the one to compare with
   * @return true if so
   */
  public boolean xmlDifferentBusinessProperties(T other);

  /**
   * save the business properties (not update properties)
   * @param existingRecord null if insert, the object if exists in DB
   * generally just copy the hibernate version number, and last updated to the
   * object and store it
   * @return the new object or existing
   */
  public T xmlSaveBusinessProperties(T existingRecord);

  /**
   * save the udpate properties (e.g. last updated).  Note, this is
   * done with a sql update statement, not with hibernate
   */
  public void xmlSaveUpdateProperties();
  
  /**
   * copy business (non update) properties to an existing record
   * @param existingRecord
   */
  public void xmlCopyBusinessPropertiesToExisting(T existingRecord);

  /** set id key in db 
   * @return id
   */
  public String xmlGetId();

  /** set id key in db 
   * @param theId 
   */
  public void xmlSetId(String theId);
  
}
