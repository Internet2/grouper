/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups;

import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasPaging;


/**
 * Paging objects helps return a partial resultset and communicate
 * which records are being returned
 * @author mchyzer
 *
 */
public class AsasApiPaging {

  /**
   * convert the api beans to the transport beans
   * @param asasApiGroups
   * @return the api bean
   */
  public static AsasPaging convertTo(AsasApiPaging asasApiPaging) {
    if (asasApiPaging == null) {
      return null;
    }
    AsasPaging asasPaging = new AsasPaging();
    asasPaging.setAscending(asasApiPaging.isAscending());
    asasPaging.setCount(asasApiPaging.getCount());
    asasPaging.setOffset(asasApiPaging.getOffset());
    asasPaging.setSortField(asasApiPaging.getSortField());
    asasPaging.setTotalCount(asasApiPaging.getTotalCount());
    return asasPaging;
  }

  /** record 0-indexed to start with... second page would be offset 100 */
  private long offset;

  /** number of records that are being returned */
  private long count;

  /** field name is dependent on the search, e.g. displayName */
  private String sortField;

  /** true or false sorting ascending or descending */
  private boolean ascending;

  /** total number of records (not just the ones being returned, but overall) */
  private long totalCount;

  
  /**
   * record 0-indexed to start with... second page would be offset 100
   * @return the offset
   */
  public long getOffset() {
    return this.offset;
  }

  
  /**
   * record 0-indexed to start with... second page would be offset 100
   * @param offset1 the offset to set
   */
  public void setOffset(long offset1) {
    this.offset = offset1;
  }

  
  /**
   * number of records that are being returned
   * @return the count
   */
  public long getCount() {
    return this.count;
  }

  
  /**
   * number of records that are being returned
   * @param count1 the count to set
   */
  public void setCount(long count1) {
    this.count = count1;
  }

  
  /**
   * field name is dependent on the search, e.g. displayName
   * @return the sortField
   */
  public String getSortField() {
    return this.sortField;
  }

  
  /**
   * field name is dependent on the search, e.g. displayName
   * @param sortField1 the sortField to set
   */
  public void setSortField(String sortField1) {
    this.sortField = sortField1;
  }

  
  /**
   * true or false sorting ascending or descending
   * @return the ascending
   */
  public boolean isAscending() {
    return this.ascending;
  }

  
  /**
   * true or false sorting ascending or descending
   * @param ascending1 the ascending to set
   */
  public void setAscending(boolean ascending1) {
    this.ascending = ascending1;
  }

  
  /**
   * total number of records (not just the ones being returned, but overall)
   * @return the totalCount
   */
  public long getTotalCount() {
    return this.totalCount;
  }

  
  /**
   * @param totalCount1 the totalCount to set
   */
  public void setTotalCount(long totalCount1) {
    this.totalCount = totalCount1;
  }
  
  
}
