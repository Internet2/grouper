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
 * $Id: QuerySortField.java,v 1.2 2009-04-13 16:53:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

/**
 * simple bean holds one sort field
 */
public class QuerySortField {

  /**
   * col or hib field to sort on 
   */
  private String column;
  
  /** 
   * if ascending or not (default true) 
   */
  private boolean ascending = true;

  /**
   * @param column1
   * @param ascending1
   */
  public QuerySortField(String column1, boolean ascending1) {
    this.column = column1;
    this.ascending = ascending1;
  }

  /**
   * col or hib field to sort on 
   * @return col
   */
  public String getColumn() {
    return this.column;
  }

  /**
   * col or hib field to sort on 
   * @param column1
   */
  public void setColumn(String column1) {
    this.column = column1;
  }

  /**
   * col or hib field to sort on 
   * @return if ascending
   */
  public boolean isAscending() {
    return this.ascending;
  }

  /**
   * col or hib field to sort on 
   * @param ascending1
   */
  public void setAscending(boolean ascending1) {
    this.ascending = ascending1;
  }
  
}
