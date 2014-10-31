package edu.internet2.middleware.grouperVoot.beans;


/**
 * Bean to respresent a valid VOOT response.
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 *
 */
public abstract class VootResponse {

  /**
   * Paginate the results ad assign the values to pagination attributes
   * @param resultArray the compelte resultset from Grouper
   * @param start the index of the first element to be put into result
   * @param count the number of elements to be put into result
   * @return the new collection containing the right page for the results 
   */
  public void paginate(Object[] resultArray, int start, int count) {
    if (resultArray == null) {
    	this.startIndex = 0;
        this.totalResults = 0;
        this.itemsPerPage = 0;
    }
    else {
      this.startIndex = start;
      this.totalResults = resultArray.length;
      this.itemsPerPage = count;
    }
  }
  
  /** starts with 0 */
  protected Integer startIndex;

  /** total number of results */
  protected Integer totalResults;

  /**
   * number of items in a page
   */
  protected Integer itemsPerPage;

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
