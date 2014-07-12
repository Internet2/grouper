/*******************************************************************************
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.subject.provider;


/**
 * bean that represents how the source should augment the query for the status.
 * note, it the field name and datastore are blank, then just use the stripped query
 * for this datastore
 * 
 * @author mchyzer
 */
public class SubjectStatusResult {

  /**
   * 
   */
  public SubjectStatusResult() {
    super();
  }

  /**
   * if this is a search for all
   */
  private boolean all = false;

  /**
   * if this is a search for all
   * @return if all
   */
  public boolean isAll() {
    return this.all;
  }

  /**
   * if this is a search for all
   * @param all1
   */
  public void setAll(boolean all1) {
    this.all = all1;
  }

  /**
   * whether it is equals or not equals
   */
  private boolean equals;
  
  /**
   * if there was a status part of the query, strip that out, and this is the remaining part
   */
  private String strippedQuery;
  
  /**
   * the value of the status to use for this query
   */
  private String datastoreValue;
  
  /** 
   * the field name (column or attribute) of the status to use for this query 
   */
  private String datastoreFieldName;

  
  
  /**
   * whether it is equals or not equals
   * @return
   */
  public boolean isEquals() {
    return this.equals;
  }

  /**
   * whether it is equals or not equals
   * @param equals1
   */
  public void setEquals(boolean equals1) {
    this.equals = equals1;
  }

  /**
   * if there was a status part of the query, strip that out, and this is the remaining part
   * @return status
   */
  public String getStrippedQuery() {
    return this.strippedQuery;
  }

  /**
   * if there was a status part of the query, strip that out, and this is the remaining part
   * @param strippedQuery1
   */
  public void setStrippedQuery(String strippedQuery1) {
    this.strippedQuery = strippedQuery1;
  }

  /**
   * the value of the status to use for this query
   * @return datastore value
   */
  public String getDatastoreValue() {
    return this.datastoreValue;
  }

  /**
   * the value of the status to use for this query
   * @param datastoreValue1
   */
  public void setDatastoreValue(String datastoreValue1) {
    this.datastoreValue = datastoreValue1;
  }

  /**
   * the field name (column or attribute) of the status to use for this query 
   * @return field name
   */
  public String getDatastoreFieldName() {
    return this.datastoreFieldName;
  }

  /**
   * the field name (column or attribute) of the status to use for this query 
   * @param datastoreFieldName1
   */
  public void setDatastoreFieldName(String datastoreFieldName1) {
    this.datastoreFieldName = datastoreFieldName1;
  }

  
  
}
