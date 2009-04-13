/*
 * @author mchyzer
 * $Id: QuerySort.java,v 1.2 2009-04-13 16:53:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class QuerySort {

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
  private int maxCols = 2;

  /**
   * max cols to store
   * @return max cols
   */
  public int getMaxCols() {
    return this.maxCols;
  }

  /**
   * max cols to store
   * @param maxCols
   */
  public void setMaxCols(int maxCols) {
    this.maxCols = maxCols;
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
    this.querySortFields.add(new QuerySortField(column, ascending));
  }

  /**
   * 
   * @param column
   * @param ascending
   */
  public void assignSort(String column, boolean ascending) {
    this.querySortFields.clear();
    this.querySortFields.add(new QuerySortField(column, ascending));
  }
  
  /**
   * 
   * @param column
   * @param ascending
   */
  public void addSort(String column, boolean ascending) {
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
