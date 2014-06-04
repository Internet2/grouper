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

import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * paging object holds state of next.previous etc
 */
@SuppressWarnings("serial")
public class GuiPaging implements Serializable {

  /** page size */
  private int pageSize;
  
  /**
   * page number indexed by 1 (friendly) 
   */
  private int pageNumber = 1;
  
  /**
   * total record count in results
   */
  private int totalRecordCount;
  
  
  /**
   * query paging bean
   * @return query paging bean
   */
  public QueryPaging queryPaging() {
    QueryPaging queryPaging = new QueryPaging();
    queryPaging.setPageNumber(this.pageNumber);
    queryPaging.setPageSize(this.pageSize);
    queryPaging.setTotalRecordCount(this.totalRecordCount);
    queryPaging.calculateIndexes();
    return queryPaging;
  }
  
  /**
   * total record count in results
   * @return the totalRecordCount
   */
  public int getTotalRecordCount() {
    return this.totalRecordCount;
  }

  
  /**
   * total record count in results
   * @param totalRecordCount1 the totalRecordCount to set
   */
  public void setTotalRecordCount(int totalRecordCount1) {
    this.totalRecordCount = totalRecordCount1;
  }

  /**
   * getter for pageNumber: page number indexed by 1 (friendly)
   * @return the value of the field
   */
  public int getPageNumber() {
    return this.pageNumber;
  }
  
  /**
   * getter for pageSize: the number of records per page
   * @return the value of the field
   */
  public int getPageSize() {
    return this.pageSize;
  }
  
  /**
   * setter for pageSize: the number of records per page
   * @param _pageSize is the data to set
   */
  public void setPageSize(int _pageSize) {
    this.pageSize = _pageSize;
  }
  
  /**
   * 
   * @param pageNumber1
   */
  public void setPageNumber(int pageNumber1) {
    this.pageNumber = pageNumber1;
  }

  /**
   * this is for the ui v1
   * init a paging object, if it exists, update the page size
   * @param pagingName 
   */
  public static void init(String pagingName) {
    
    GuiPaging guiPaging = new GuiPaging();
    
    //lets see what the default page size is
    String pageSizeDefaultString = GrouperUiConfig.retrieveConfig().propertyValueString("pager.pagesize.default");
    
    guiPaging.setPageSize(GrouperUtil.intValue(pageSizeDefaultString));

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    //setup a paging bean to send to screen
    guiResponseJs.addPager(pagingName, guiPaging);
  }

  /**
   * find a paging in response or app state, this is for the ui v1
   * @param pagingName
   * @param exceptionIfNotFound
   * @return the pager
   */
  public static GuiPaging retrievePaging(String pagingName, boolean exceptionIfNotFound) {
    //we need to find the pager, either it is something we are initializing, or something sent from browser
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    GuiPaging guiPaging = (GrouperUtil.nonNull(guiResponseJs.getPagers())).get(pagingName);
    if (guiPaging == null) {
      
      AppState appState = AppState.retrieveFromRequest();
      guiPaging = (GrouperUtil.nonNull(appState.getPagers())).get(pagingName);
      
    }
    
    if (guiPaging == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find pager: '" + pagingName + "' it needs to exist on the screen or be initted");
    }
    return guiPaging;
  }
  
}
