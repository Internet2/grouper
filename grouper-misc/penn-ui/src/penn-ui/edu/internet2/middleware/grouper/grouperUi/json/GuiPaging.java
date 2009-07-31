/*
 * @author mchyzer
 * $Id: GuiPaging.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

/**
 *
 */
public class GuiPaging {

  /** number of results on page */
  private int numberOfResultsOnPage;
  
  /** is first page */
  private boolean isFirstPage;
  
  /** is last page */
  private boolean isLastPage;
  
  /**
   * number of pages total 
   */
  private int numberOfPages = -1;
  
  /**
   * index (1 indexed) of the last record on the page 
   */
  private int pageEndIndex = -1;

  /**
   * page number indexed by 1 (friendly) 
   */
  private int pageNumber = 1;
  
  /**
   * the number of records per page 
   */
  private int pageSize = 30;
  
  /**
   * index of the first record on the first page (1 indexed, friendly) 
   */
  private int pageStartIndex = -1;
  
  /**
   * total number of records in the set (you must set this before the tag is called) 
   */
  private int totalRecordCount = -1;
  
  /** total number of results on last page */
  private int totalOnLastPage = -1;
  
  /** first index on page 0 indexed */
  private int firstIndexOnPage = -1;
  
  /** last index on page 0 indexed */
  private int lastIndexOnPage = -1;
  
  /**
   * getter for numberOfPages: number of pages total
   * @return the value of the field
   */
  public int getNumberOfPages() {
    return this.numberOfPages;
  }
  
  /**
   * getter for pageEndIndex: index (1 indexed) of the last record on the page
   * @return the value of the field
   */
  public int getPageEndIndex() {
    return this.pageEndIndex;
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
   * getter for pageStartIndex: index of the first record on the first page (1 indexed, friendly)
   * @return the value of the field
   */
  public int getPageStartIndex() {
    return this.pageStartIndex;
  }
  
  /**
   * getter for totalRecordCount: total number of records in the set (you must set this before the tag is called)
   * @return the value of the field
   */
  public int getTotalRecordCount() {
    return this.totalRecordCount;
  }
  
  /**
   * setter for numberOfPages: number of pages total
   * @param _numberOfPages is the data to set
   */
  public void setNumberOfPages(int _numberOfPages) {
    this.numberOfPages = _numberOfPages;
  }
  
  /**
   * setter for pageEndIndex: index (1 indexed) of the last record on the page
   * @param _pageEndIndex is the data to set
   */
  public void setPageEndIndex(int _pageEndIndex) {
    this.pageEndIndex = _pageEndIndex;
  }
  
  /**
   * setter for pageSize: the number of records per page
   * @param _pageSize is the data to set
   */
  public void setPageSize(int _pageSize) {
    this.pageSize = _pageSize;
  }
  
  /**
   * setter for pageStartIndex: index of the first record on the first page (1 indexed, friendly)
   * @param _pageStartIndex is the data to set
   */
  public void setPageStartIndex(int _pageStartIndex) {
    this.pageStartIndex = _pageStartIndex;
  }
  
  /**
   * setter for totalRecordCount: total number of records in the set (you must set this before the tag is called)
   * @param _totalRecordCount is the data to set
   */
  public void setTotalRecordCount(int _totalRecordCount) {
    this.totalRecordCount = _totalRecordCount;
  }

  /**
   * number of results on page
   * @return number of results
   */
  public int getNumberOfResultsOnPage() {
    return this.numberOfResultsOnPage;
  }

  /**
   * number of results on page
   * @param numberOfResultsOnPage1
   */
  public void setNumberOfResultsOnPage(int numberOfResultsOnPage1) {
    this.numberOfResultsOnPage = numberOfResultsOnPage1;
  }

  /**
   * if first page
   * @return if first page
   */
  public boolean isFirstPage() {
    return this.isFirstPage;
  }

  /**
   * if first page
   * @param isFirstPage1
   */
  public void setFirstPage(boolean isFirstPage1) {
    this.isFirstPage = isFirstPage1;
  }

  /**
   * if last page
   * @return if
   */
  public boolean isLastPage() {
    return this.isLastPage;
  }

  /**
   * last page
   * @param isLastPage1
   */
  public void setLastPage(boolean isLastPage1) {
    this.isLastPage = isLastPage1;
  }

  /**
   * total on last page
   * @return total on last page
   */
  public int getTotalOnLastPage() {
    return this.totalOnLastPage;
  }

  /**
   * total
   * @param totalOnLastPage1
   */
  public void setTotalOnLastPage(int totalOnLastPage1) {
    this.totalOnLastPage = totalOnLastPage1;
  }

  /**
   * first index on page (0 indexed)
   * @return first index 
   */
  public int getFirstIndexOnPage() {
    return this.firstIndexOnPage;
  }

  /**
   * 
   * @param firstIndexOnPage1
   */
  public void setFirstIndexOnPage(int firstIndexOnPage1) {
    this.firstIndexOnPage = firstIndexOnPage1;
  }

  /**
   * 
   * @return last index on page
   */
  public int getLastIndexOnPage() {
    return this.lastIndexOnPage;
  }

  /**
   * 
   * @param lastIndexOnPage1
   */
  public void setLastIndexOnPage(int lastIndexOnPage1) {
    this.lastIndexOnPage = lastIndexOnPage1;
  }

  /**
   * 
   * @param pageNumber1
   */
  public void setPageNumber(int pageNumber1) {
    this.pageNumber = pageNumber1;
  }
  
}
