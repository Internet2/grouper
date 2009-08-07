/*
 * @author mchyzer
 * $Id: GuiPaging.java,v 1.2 2009-08-07 07:36:01 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class GuiPaging {

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
   * init a paging object, if it exists, update the page size
   * @param pagingName 
   * @param pageSize 
   */
  public static void init(String pagingName, int pageSize) {
    
    GuiPaging guiPaging = new GuiPaging();
    guiPaging.setPageSize(pageSize);

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    //setup a paging bean to send to screen
    guiResponseJs.addPager(pagingName, guiPaging);
  }

  /**
   * find a paging in response or app state
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
