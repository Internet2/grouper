package edu.internet2.middleware.grouperVoot.beans;

import edu.internet2.middleware.grouper.util.GrouperUtil;


public abstract class VootResponse {

  /**
   * assign paging for a results array
   * @param resultArray
   */
  public void assignPaging(Object resultArray) {
    
    int resultArrayLength = GrouperUtil.length(resultArray);
    
    if (resultArrayLength == 0) {
      
      this.startIndex = 0;
      this.totalResults = 0;
      this.itemsPerPage = 0;
      
    } else {
      
      this.startIndex = 0;
      this.totalResults = resultArrayLength;
      this.itemsPerPage = resultArrayLength;
      
    }
    
  }
  
  /** starts with 0 */
  private Integer startIndex;

  /** total number of results */
  private Integer totalResults;

  /**
   * number of items in a page
   */
  private Integer itemsPerPage;

  /**
   * starts with 0
   * @return start index
   */
  public Integer getStartIndex() {
    return this.startIndex;
  }

  /**
   * starts with 0
   * @param startIndex1
   */
  public void setStartIndex(Integer startIndex1) {
    this.startIndex = startIndex1;
  }

  /**
   * total number of results
   * @return total number of results
   */
  public Integer getTotalResults() {
    return this.totalResults;
  }

  /**
   * total number of results
   * @param totalResults1
   */
  public void setTotalResults(Integer totalResults1) {
    this.totalResults = totalResults1;
  }

  /**
   * number of items in a page
   * @return number of items in a page
   */
  public Integer getItemsPerPage() {
    return this.itemsPerPage;
  }

  /**
   * number of items in a page
   * @param itemsPerPage1
   */
  public void setItemsPerPage(Integer itemsPerPage1) {
    this.itemsPerPage = itemsPerPage1;
  }

  
  
}
