/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans;

import edu.internet2.middleware.tierApiAuthzServer.corebeans.TaasMeta;
import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.AsasApiQueryParams;
import edu.internet2.middleware.tierApiAuthzServer.j2ee.AsasHttpServletRequest;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


/**
 * Paging objects helps return a partial resultset and communicate
 * which records are being returned.  Note, when passed off the 
 * interface implementation, all the minimums, and defaults, etc are
 * already calculated.  All you  need to do is use the object if it is there,
 * and it will be all filled out.  Or dont use it.  You dont need to 
 * set minimums or defaults.
 * 
 * @author mchyzer
 *
 */
public class AsasApiQueryParams {

  /**
   * validate a query params object
   */
  public void validate() {
    if (this.offset != null && this.limit == null) {
      throw new AsasRestInvalidRequest("You cannot have an offset if not sending a limit");
    }
    if (this.offset != null && this.offset < 0) {
      throw new AsasRestInvalidRequest("You cannot have an offset less than 0");
    }
    if (this.limit != null && this.limit < 1) {
      throw new AsasRestInvalidRequest("You cannot have a limit less than 1");
    }
    if (this.offset != null && !StandardApiServerUtils.isBlank(this.offsetFieldValue)) {
      throw new AsasRestInvalidRequest("You cannot have an offset and an offsetFieldValue");
    }
  }
  
  /** if we should do the total count in a paged query */
  private Boolean doTotalCount;
  
  /**
   * if we should do the total count in a paged query
   * @return the doTotalCount
   */
  public Boolean getDoTotalCount() {
    return this.doTotalCount;
  }
  
  /**
   * if we should do the total count in a paged query
   * @param doTotalCount1 the doTotalCount to set
   */
  public void setDoTotalCount(Boolean doTotalCount1) {
    this.doTotalCount = doTotalCount1;
  }

  /** with paging, retrieve records after this value */
  private String offsetFieldValue;

  /**
   * with paging, retrieve records after this value
   * @return the offsetFieldValue
   */
  public String getOffsetFieldValue() {
    return this.offsetFieldValue;
  }
  
  /**
   * with paging, retrieve records after this value
   * @param offsetFieldValue1 the offsetFieldValue to set
   */
  public void setOffsetFieldValue(String offsetFieldValue1) {
    this.offsetFieldValue = offsetFieldValue1;
  }

  /**
   * get an asas api query params from the threadlocal query string.
   * Note, you still need to see if paging is enabled
   * @return the query params
   */
  public static AsasApiQueryParams convertFromQueryString() {
    
    AsasHttpServletRequest asasHttpServletRequest = AsasHttpServletRequest.retrieve();
    AsasApiQueryParams asasApiQueryParams = new AsasApiQueryParams();
    
    {
      Long limit = asasHttpServletRequest.getParameterLong("limit");
      asasApiQueryParams.setLimit(limit);
    }
    
    {
      Long offset = asasHttpServletRequest.getParameterLong("offset");
      asasApiQueryParams.setOffset(offset);
    }
    
    {
      String offsetFieldValue = asasHttpServletRequest.getParameter("offsetFieldValue");
      asasApiQueryParams.setOffsetFieldValue(offsetFieldValue);
    }

    {
      String sortField = asasHttpServletRequest.getParameter("sortField");
      asasApiQueryParams.setSortField(sortField);
    }
    
    {
      Boolean ascending = asasHttpServletRequest.getParameterBoolean("ascending");
      asasApiQueryParams.setAscending(ascending);
    }
    
    {
      boolean doTotalCount = StandardApiServerUtils.equals(asasHttpServletRequest.getParameter("extraFields"),
        "meta.totalCount");
      asasApiQueryParams.setDoTotalCount(doTotalCount);
    }
    
    asasApiQueryParams.validate();
    
    return asasApiQueryParams;
  }
  
  /**
   * convert an asas paging to an asas api paging
   * @param asasPaging
   * @return the asas api paging
   */
  public static AsasApiQueryParams convertTo(TaasMeta asasMeta) {
    if (asasMeta == null) {
      return null;
    }
    
    //do some logic?
    
    AsasApiQueryParams asasApiPaging = new AsasApiQueryParams();
    asasApiPaging.setAscending(asasMeta.getAscending());
    asasApiPaging.setLimit(asasMeta.getLimit());
    asasApiPaging.setOffset(asasMeta.getOffset());
    asasApiPaging.setSortField(asasMeta.getSortField());
    asasApiPaging.setTotalCount(asasMeta.getTotalCount());
    return asasApiPaging;
  }
  
  
  
  /**
   * convert the api beans to the transport beans
   * @param asasApiGroups
   * @return the api bean
   */
  public static void convertTo(AsasApiQueryParams asasApiPaging, TaasMeta asasMeta) {
    if (asasApiPaging == null) {
      return;
    }
    asasMeta.setAscending(asasApiPaging.getAscending());
    asasMeta.setLimit(asasApiPaging.getLimit());
    asasMeta.setOffset(asasApiPaging.getOffset());
    asasMeta.setSortField(asasApiPaging.getSortField());
    asasMeta.setTotalCount(asasApiPaging.getTotalCount());
    asasMeta.setOffsetFieldValue(asasApiPaging.getOffsetFieldValue());
  }

  /** record 0-indexed to start with... second page would be offset 100 */
  private Long offset;

  /** number of records that are being returned */
  private Long limit;

  /** field name is dependent on the search, e.g. displayName */
  private String sortField;

  /** true or false sorting ascending or descending */
  private Boolean ascending;

  /** total number of records (not just the ones being returned, but overall) */
  private Long totalCount;

  
  /**
   * record 0-indexed to start with... second page would be offset 100
   * @return the offset
   */
  public Long getOffset() {
    return this.offset;
  }

  
  /**
   * record 0-indexed to start with... second page would be offset 100
   * @param offset1 the offset to set
   */
  public void setOffset(Long offset1) {
    this.offset = offset1;
  }

  
  /**
   * number of records that are being returned
   * @return the limit
   */
  public Long getLimit() {
    return this.limit;
  }

  
  /**
   * number of records that are being returned
   * @param count1 the limit to set
   */
  public void setLimit(Long count1) {
    this.limit = count1;
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
  public Boolean getAscending() {
    return this.ascending;
  }

  
  /**
   * true or false sorting ascending or descending
   * @param ascending1 the ascending to set
   */
  public void setAscending(Boolean ascending1) {
    this.ascending = ascending1;
  }

  
  /**
   * total number of records (not just the ones being returned, but overall)
   * @return the totalCount
   */
  public Long getTotalCount() {
    return this.totalCount;
  }

  
  /**
   * @param totalCount1 the totalCount to set
   */
  public void setTotalCount(Long totalCount1) {
    this.totalCount = totalCount1;
  }
  
  
}
