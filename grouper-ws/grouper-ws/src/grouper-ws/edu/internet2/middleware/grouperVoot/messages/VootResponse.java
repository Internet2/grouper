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

package edu.internet2.middleware.grouperVoot.messages;

/**
 * Bean to respresent a valid VOOT response.
 * 
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 */
public abstract class VootResponse {
  /** Start index starts with 0 */
  protected Integer startIndex;
  
  /** Total number of results */
  protected Integer totalResults;
  
  /** Number of items in a page */
  protected Integer itemsPerPage;

  /**
   * Paginate the results ad assign the values to pagination attributes.
   * 
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
    } else {
      this.startIndex = (start > 0 ) ? start : 0;
      this.totalResults = resultArray.length;
      if (count > 0) {
        //note Integer.min is in Java7, so lets use Math.min so Java6 will work
        this.itemsPerPage = Math.min(count, resultArray.length - start);
      }
      else {
        this.itemsPerPage = resultArray.length;
      }
    }
  }


  /**
   * Get start index, starts with 0
   * 
   * @return the start index
   */
  public Integer getStartIndex() {
    return this.startIndex;
  }

  /**
   * Set start index, starts with 0
   * 
   * @param startIndex1 the start index
   */
  public void setStartIndex(Integer startIndex1) {
    this.startIndex = startIndex1;
  }

  /**
   * Get the total number of results.
   * 
   * @return the total number of results.
   */
  public Integer getTotalResults() {
    return this.totalResults;
  }

  /**
   * Set the total number of results.
   * 
   * @param totalResults1 the total number of results.
   */
  public void setTotalResults(Integer totalResults1) {
    this.totalResults = totalResults1;
  }

  /**
   * Get the number of items in a page.
   * 
   * @return the number of items in a page.
   */
  public Integer getItemsPerPage() {
    return this.itemsPerPage;
  }

  /**
   * Set the number of items in a page.
   * 
   * @param itemsPerPage1 the number of items in a page.
   */
  public void setItemsPerPage(Integer itemsPerPage1) {
    this.itemsPerPage = itemsPerPage1;
  }

}
