/**
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
 */
/*
 * @author mchyzer
 * $Id: QueryPaging.java,v 1.4 2009-11-13 07:32:43 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class QueryPaging {

  /**
   * if we should do the total count when we do that actual query
   * (note, this might not always be possible in all cases, will throw an
   * exception if not possible)
   */
  private boolean doTotalCount = false;
  
  /**
   * if we should cache the total count and not run again if already run
   */
  private boolean cacheTotalCount = true;
  
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
   * getter for pageStartIndex: index of the first record on the first page, this was documented as 1 indexed, but it seems to be 0 indexed
   */
  private int pageStartIndex = -1;
  
  /**
   * total number of records in the set (you must set this before the tag is called) 
   */
  private int totalRecordCount = -1;
  
  /**
   * 
   */
  public QueryPaging() {
    super();
  }

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
   * getter for pageStartIndex: index of the first record on the first page, this was documented as 1 indexed, but it seems to be 0 indexed
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
    //cant be 0 or negative...
    if (_pageSize < 1) {
      _pageSize = 1;
    }
    this.pageSize = _pageSize;
  }
  
  /**
   * setter for pageStartIndex: index of the first record on the first page, this was documented as 1 indexed, but it seems to be 0 indexed
   * @param _pageStartIndex is the data to set
   */
  public void setPageStartIndex(int _pageStartIndex) {
    this.pageStartIndex = _pageStartIndex;
  }
  
  /**
   * <pre>
   * set the first index on the page, 0 indexed, dont use pages to query...
   * </pre>
   * @param startIndex
   */
  public void setPageStartIndexQueryByIndex(int startIndex) {
    this.pageNumber = -1;
    this.pageStartIndex = startIndex;
  }

  /**
   * setter for totalRecordCount: total number of records in the set (you must set this before the tag is called)
   * @param _totalRecordCount is the data to set
   */
  public void setTotalRecordCount(int _totalRecordCount) {
    this.totalRecordCount = _totalRecordCount;
  }

  /**
   * throw exception if this bean is not initted
   */
  public void assertInitted() {
    if (!this.initted()) {
      throw new RuntimeException(
          "QueryPaging bean needs to be initted and calculated! " + this);
    }
  }

  /**
   * Based on the pageNumber, pageSize, and totalRecordCount, figure out the rest.
   * Pretty much everything is friendly / oracle indexed (by 1)
   */
  public void calculateIndexes() {
  
    //see if total record count is set
    if (this.getTotalRecordCount() < 0) {
      throw new RuntimeException("Total count must be set before calculating paging!");
    }
  
    //first figure out number of pages
    int pages = numberOfPages();
    int totalOnLastPage = getTotalOnLastPage();
  
    this.setNumberOfPages(pages);
  
    //are we over the limit?  make sure no
    if (this.getPageNumber() > pages) {
      this.setPageNumber(pages);
    }
    //cant be on page 0
    if (this.getPageNumber() == 0) {
      this.setPageNumber(1);
    }
  
    //it is a special case to have no records
    if (this.getTotalRecordCount() == 0) {
      this.setPageStartIndex(0);
      this.setPageEndIndex(0);
    } else {
      this.setPageStartIndex(((this.getPageNumber() - 1) * this.getPageSize()) + 1);
  
      //see if we are on the last page
      if (this.getPageNumber() == pages) {
        this.setPageEndIndex(this.getPageStartIndex() + totalOnLastPage - 1);
      } else {
        this.setPageEndIndex(this.getPageStartIndex() + this.getPageSize() - 1);
      }
    }
  
  }

  /**
   * This can be used to provide a drop down box of possible pages to skip to.  This 
   * is helpful for example if there are 30 pages and you want page 15, normally this
   * would require numerous clicks before page 15 is displayed as a choice.
   *  
   * @return a list of all the valid page numbers that could be referred to
   */
  public List<Integer> getAllPages() {
    
    List<Integer> result = new ArrayList<Integer>(this.getNumberOfPages());
    for (int counter=0; counter < this.getNumberOfPages(); ++counter) {
      result.add(counter + 1);
    }
    
    return result;
  }

  /**
   * return the first index on page (0 indexed), from 0
   * to the number of results
   * @return the first index on page 0 indexed
   */
  public int getFirstIndexOnPage() {
    
    if (this.pageNumber < 0 && this.pageStartIndex > 0) {
      return this.pageStartIndex - 1;
    }
    
    return (this.getPageNumber() - 1) * this.getPageSize();
  }

  /**
   * <pre>
   * set the first index on the page, 0 indexed
   * 0 -> 1, pageSize -> 2, 2*pageSize -> 3
   * </pre>
   * @param startIndex
   */
  public void setFirstIndexOnPage(int startIndex) {
    if (this.pageSize <= 0) {
      throw new RuntimeException("Problem, pagesize must be greater than 0: " + this.pageSize);
    }
    //lets calculate the start index.  0 -> 1, pageSize -> 2, 2*pageSize -> 3
    this.pageNumber = (startIndex / pageSize) + 1;
  }
    
  /**
   * return the last index on page (0 indexed)
   * @return the last index on page 0 indexed
   */
  public int getLastIndexOnPage() {
  
    if (isLastPage()) {
      return this.getTotalRecordCount() - 1;
    }
    return (this.getPageNumber() * this.getPageSize()) - 1;
  }

  /**
   * Get the number of results on the current page
   * @return number of results on current page
   */
  public int getNumberOfResultsOnPage() {
    return (this.getLastIndexOnPage() - this.getFirstIndexOnPage()) + 1;
  }

  /**
   * calculate the total record count on last page
   * @return total records on last page
   */
  public int getTotalOnLastPage() {
    int totalOnLastPage = -1;
  
    if (this.getTotalRecordCount() == 0) {
      totalOnLastPage = 0;
    } else {
  
      totalOnLastPage = this.getTotalRecordCount() % this.getPageSize();
  
      //there wouldnt be 0.
      if (totalOnLastPage == 0 && this.getTotalRecordCount() > 0) {
        totalOnLastPage = this.getPageSize();
      }
    }
    return totalOnLastPage;
  }

  /**
   * see if this bean has been calculated
   * @return true if initted
   */
  public boolean initted() {
    return !( this.getPageStartIndex() == -1
      || this.getTotalRecordCount() == -1
      || this.getPageEndIndex() == -1
      || this.getNumberOfPages() == -1);
    
  }

  /**
   * see if the paging is on first page
   * 
   * @return true if first page
   */
  public boolean isFirstPage() {
    return this.getPageNumber() == 1;
  }

  /**
   * see if the paging is on last page
   * 
   * @return true if last page
   */
  public boolean isLastPage() {
    return this.getTotalRecordCount() == 0
        || this.getPageNumber() == this.getNumberOfPages();
  }

  /**
   * based on a paging bean, and a current page, return the next page needed
   * to display (ellipses, button, or label)
   * @param currentPageNumber
   * @return the next page number which is relevant
   */
  public int nextPageNeeded(int currentPageNumber) {
    //if total is less than 11, then increment
    if (this.getNumberOfPages() <= 11) {
      return currentPageNumber + 1;
    }
  
    //if it is two away from start
    if (currentPageNumber - 1 < 4) {
      return currentPageNumber + 1;
    }
  
    //if it is two away from end
    if (this.getNumberOfPages() - currentPageNumber < 4) {
      return currentPageNumber + 1;
    }
  
    //if it is 2 away from page we are on
    if (Math.abs(this.getPageNumber() - currentPageNumber) < 4) {
      return currentPageNumber + 1;
    }
  
    //now comes the trick, find the next page number which is relevant
    if (currentPageNumber < this.getPageNumber()) {
      return this.getPageNumber() - 3;
    }
  
    //else we are going to the end
    return this.getNumberOfPages() - 3;
  
  }

  /**
   * Get the number of pages total
   * 
   * @return the number of pages total
   */
  private int numberOfPages() {
    int pages = this.getPageSize() == 0 ? 1 : this.getTotalRecordCount() / this.getPageSize();
    int totalOnLastPage = this.getPageSize() == 0 ? 0 : this.getTotalRecordCount() % this.getPageSize();
    if (totalOnLastPage > 0) {
      pages++;
    }
    if (this.getTotalRecordCount() == 0) {
      pages = 1;
    }
    return pages;
  }

  /**
   * pageNumber: page number indexed by 1 (friendly)
   * @param _pageNumber 
   */
  public void setPageNumber(int _pageNumber) {
    if (_pageNumber < 1) {
      throw new RuntimeException("Cannot set the page number to " +
          "less than 1 or more than number of pages (" 
          + this.getNumberOfPages() + "): " + _pageNumber);
    }
    this.pageNumber = _pageNumber;
  }

  /**
   * if we should page
   * @return if should page
   */
  public boolean shouldPage() {
    return this.getPageSize() >= 0 && this.getPageNumber() >= 0;
  }

  /**
   * factory for query paging
   * @param pageSize
   * @param pageNumber 1 indexed page number
   * @param doTotalCount true to do total count, false to not
   * @return the query paging
   */
  public static QueryPaging page(int pageSize, int pageNumber, boolean doTotalCount) {
    if (pageSize == -1 && pageNumber <= 0) {
      return null;
    }
    return new QueryPaging(pageSize, pageNumber, doTotalCount);
  }

  /**
   * constructor.  NOTE, THIS IS 1 INDEXED ON PAGE NUMBER
   * @param pageSize1 number of records per page
   * @param pageNumber1 1 indexed page number to show
   * @param doTotalCount1 if hibernate session should do a total count and
   * calculate indexes when doing the query
   */
  public QueryPaging(int pageSize1, int pageNumber1, boolean doTotalCount1) {
    this.pageSize = pageSize1;
    this.pageNumber = pageNumber1;
    this.doTotalCount = doTotalCount1;
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "pageSize: " + this.pageSize + ", pageNumberOnIndexed: " + this.pageNumber;
  }

  /**
   * if we should do the total count when we do that actual query
   * (note, this might not always be possible in all cases, will throw an
   * exception if not possible)
   * @return true if we should do a total count
   */
  public boolean isDoTotalCount() {
    return this.doTotalCount;
  }

  /**
   * if we should do the total count when we do that actual query
   * (note, this might not always be possible in all cases, will throw an
   * exception if not possible)
   * @param doTotalCount1
   */
  public void setDoTotalCount(boolean doTotalCount1) {
    this.doTotalCount = doTotalCount1;
  }

  /**
   * if we should cache the total count and not run again if already run
   * @return if we should cache the total count
   */
  public boolean isCacheTotalCount() {
    return this.cacheTotalCount;
  }

  /**
   * if we should cache the total count and not run again if already run
   * @param cacheTotalCount1
   */
  public void setCacheTotalCount(boolean cacheTotalCount1) {
    this.cacheTotalCount = cacheTotalCount1;
  }
  
}
