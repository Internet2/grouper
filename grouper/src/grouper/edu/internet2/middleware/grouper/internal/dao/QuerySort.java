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
 * $Id: QuerySort.java,v 1.2 2009-04-13 16:53:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class QuerySort {

  /**
   * 
   */
  private QuerySort() {
    
  }
  
  /**
   * 
   * @return another
   */
  public QuerySort clone() {
    QuerySort querySort = new QuerySort();
    querySort.maxCols = this.maxCols;
    querySort.querySortFields = this.querySortFields == null ? null : new ArrayList<QuerySortField>();
    for (QuerySortField querySortField : GrouperUtil.nonNull(this.querySortFields)) {
      querySort.querySortFields.add(new QuerySortField(querySortField.getColumn(), querySortField.isAscending()));
    }
    return querySort;
  }
  
  /** 
   * list of sort fields... generally it would just be one 
   */
  private List<QuerySortField> querySortFields = new ArrayList<QuerySortField>();

  /**
   * list of sort fields... generally it would just be one
   * @return the sort fields
   */
  public List<QuerySortField> getQuerySortFields() {
    return this.querySortFields;
  } 
  
  /** max cols to store */
  private int maxCols = 10;

  /**
   * max cols to store
   * @return max cols
   */
  public int getMaxCols() {
    return this.maxCols;
  }

  /**
   * max cols to store
   * @param maxCols1
   */
  public void setMaxCols(int maxCols1) {
    this.maxCols = maxCols1;
  }

  /**
   * shortcut for ascending col
   * @param column
   * @return the query sort
   */
  public static QuerySort asc(String column) {
    return new QuerySort(column, true);
  }
  
  /**
   * shortcut for descending col
   * @param column
   * @return the query sort
   */
  public static QuerySort desc(String column) {
    return new QuerySort(column, false);
  }
  
  /**
   * 
   * @param column
   * @param ascending
   */
  public QuerySort(String column, boolean ascending) {
    String[] columns = GrouperUtil.splitTrim(column, ",");
    for (int i=0;i<columns.length;i++) {
      this.querySortFields.add(new QuerySortField(columns[i], ascending));
    }
  }

  /**
   * @param column
   * @param ascending
   */
  public void assignSort(String column, boolean ascending) {
    this.querySortFields.clear();
    String[] columns = GrouperUtil.splitTrim(column, ",");
    for (int i=0;i<columns.length;i++) {
      this.querySortFields.add(new QuerySortField(columns[i], ascending));
    }
  }
  
  /**
   * 
   * @param column
   * @param ascending
   * @deprecated use insertSortToBeginning
   */
  @Deprecated
  public void addSort(String column, boolean ascending) {
    this.insertSortToBeginning(column, ascending);
  }
  
  /**
   * insert sort to beginning of sort order...
   * @param column
   * @param ascending
   */
  public void insertSortToBeginning(String column, boolean ascending) {
    Iterator<QuerySortField> iterator = this.querySortFields.iterator();
    
    //remove elements that are the same column
    while (iterator.hasNext()) {
      QuerySortField querySortField = iterator.next();
      if (StringUtils.equals(column, querySortField.getColumn())) {
        iterator.remove();
      }
    }
    
    //insert into the front of the list
    this.querySortFields.add(0, new QuerySortField(column, ascending));
    
    //max sure less than max size
    for (int i = this.querySortFields.size()-1; i >= this.maxCols; i--) {
      this.querySortFields.remove(i);
    }
  }

  /**
   * see if we are sorting
   * @return true if sorting
   */
  public boolean isSorting() {
    return this.querySortFields.size() > 0;
  }
  
  /** 
   * get the sort string based on the cols, add space before perhaps
   * @param includePreSpaceIfSorting if we should add a whitespace char before sortstring if it exists
   * @return the sort string
   */
  public String sortString(boolean includePreSpaceIfSorting) {
    
    StringBuilder result = new StringBuilder();
    for (QuerySortField querySortField : this.querySortFields) {
      
      result.append(querySortField.getColumn()).append(" ").append(querySortField.isAscending() ? "asc" : "desc").append(", ");
      
    }
    
    //remove the last comma
    if (result.length() >= 2) {
      result.delete(result.length()-2, result.length());
      
      //we are sorting if in this block, so add a space if supposed to
      if (includePreSpaceIfSorting) {
        result.insert(0, " ");
      }
    }
    
    return result.toString();
  }
  
}
