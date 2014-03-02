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
 * $Id: GuiPaging.java,v 1.3 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.json;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * sorting object holds which col is sorted
 */
@SuppressWarnings("serial")
public class GuiSorting implements Serializable {
  
  /**
   * query sort for for the DB
   */
  private QuerySort querySort = null;

  /**
   * construct
   */
  public GuiSorting() {
    
  }
  
  /**
   * construct with query sort
   * @param theQuerySort
   */
  public GuiSorting(QuerySort theQuerySort) {
    this.querySort = theQuerySort;
  }

  /**
   * query sort for for the DB
   * @return query sort
   */
  public QuerySort getQuerySort() {
    return this.querySort;
  }

  /**
   * query sort for for the DB
   * @param querySort1
   */
  public void setQuerySort(QuerySort querySort1) {
    this.querySort = querySort1;
  }

  /**
   * if is ascending
   * @return if ascending
   */
  public boolean isAscending() {
    return this.querySort.getQuerySortFields().get(0).isAscending();
  }
  
  /**
   * process the request
   * @param request
   */
  public void processRequest(HttpServletRequest request) {
    String ascendingString = request.getParameter("querySortAscending");
    if (!StringUtils.isBlank(ascendingString)) {
      boolean ascending = GrouperUtil.booleanValue(ascendingString);
      this.querySort.getQuerySortFields().get(0).setAscending(ascending);
    }
  }
  
  /**
   * css class will return sorted, sortedAsc, or empty string
   * key is which col this is
   */
  private Map<String, String> columnCssClass = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      
      if (StringUtils.equals(GuiSorting.this.querySort.getQuerySortFields().get(0).getColumn(), (String)key)) {
        if (GuiSorting.this.querySort.getQuerySortFields().get(0).isAscending()) {
          return "sortedAsc";
        }
        return "sorted";
      }
      return "";
    }

  };

  /**
   * css class will return sorted, sortedAsc, or null  (with comma)
   * key is which col this is
   * @return map
   */
  public Map<String, String> getColumnCssClass() {
    return this.columnCssClass;
  }
}
