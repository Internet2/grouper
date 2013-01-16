package edu.internet2.middleware.authzStandardApiClient.corebeans;


/**
 * This is the meta object in a response, or extend this if 
 * @author mchyzer
 *
 */
public class AsacMeta {
  
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
   * response status text code
   */
  private String status;

  /**
   * response status text code
   * @return the statusCode
   */
  public String getStatus() {
    return this.status;
  }
  
  /**
   * response status text code
   * @param statusCode1 the statusCode to set
   */
  public void setStatus(String statusCode1) {
    this.status = statusCode1;
  }


  /** 
   * if there are warnings, they will be there
   */
  private String warning = new String();

  /**
   * if there are warnings, they will be there
   * @return any warnings
   */
  public String getWarning() {
    return this.warning;
  }

  /**
   * @param resultWarnings1 the resultWarnings to set
   */
  public void setWarning(String resultWarnings1) {
    this.warning = resultWarnings1;
  }

  

  /**
   * true or false if valid request
   */
  private Boolean success;
  
  /**
   * true or false if valid request
   * @return the success
   */
  public Boolean getSuccess() {
    return this.success;
  }
  
  /**
   * true or false if valid request
   * @param success the success to set
   */
  public void setSuccess(Boolean success) {
    this.success = success;
  }

  /** timestamp this resource was last modified, e.g. 2012-10-04T03:10Z */
  private String lastModified;
  
  /**
   * timestamp this resource was last modified, e.g. 2012-10-04T03:10Z
   * @return the lastModified
   */
  public String getLastModified() {
    return this.lastModified;
  }

  
  /**
   * @param lastModified1 the lastModified to set
   */
  public void setLastModified(String lastModified1) {
    this.lastModified = lastModified1;
  }

  /**
   * Self URI to this resource 
   * e.g. https://groups.institution.edu/groupsApp/groupsApi 
   */
  private String selfUri;
  
  /**
   * Self URI to this resource 
   * e.g. https://groups.institution.edu/groupsApp/groupsApi 
   * @return the selfUri
   */
  public String getSelfUri() {
    return this.selfUri;
  }

  
  /**
   * Self URI to this resource 
   * e.g. https://groups.institution.edu/groupsApp/groupsApi 
   * @param selfUri1 the selfUri to set
   */
  public void setSelfUri(String selfUri1) {
    this.selfUri = selfUri1;
  }
  
  /** name of the structure (struct) returned, the same as the xml outer element */
  private String structureName;

  /** true or false sorting ascending or descending */
  private Boolean ascending;

  /** number of records that are being returned */
  private Long limit;

  /** record 0-indexed to start with... second page would be offset 100 */
  private Long offset;

  /** field name is dependent on the search, e.g. displayName */
  private String sortField;

  /** total number of records (not just the ones being returned, but overall) */
  private Long totalCount;

  /**
   * name of the structure (struct) returned, the same as the xml outer element
   * @return the structure name
   */
  public String getStructureName() {
    return structureName;
  }

  /**
   * name of the structure (struct) returned, the same as the xml outer element
   * @param structureName1
   */
  public void setStructureName(String structureName1) {
    this.structureName = structureName1;
  }


  /**
   * true or false sorting ascending or descending
   * @return the ascending
   */
  public Boolean getAscending() {
    return this.ascending;
  }


  /**
   * number of records that are being returned
   * @return the limit
   */
  public Long getLimit() {
    return this.limit;
  }


  /**
   * record 0-indexed to start with... second page would be offset 100
   * @return the offset
   */
  public Long getOffset() {
    return this.offset;
  }


  /**
   * field name is dependent on the search, e.g. displayName
   * @return the sortField
   */
  public String getSortField() {
    return this.sortField;
  }


  /**
   * total number of records (not just the ones being returned, but overall)
   * @return the totalCount
   */
  public Long getTotalCount() {
    return this.totalCount;
  }


  /**
   * true or false sorting ascending or descending
   * @param ascending1 the ascending to set
   */
  public void setAscending(Boolean ascending1) {
    this.ascending = ascending1;
  }


  /**
   * number of records that are being returned
   * @param count1 the limit to set
   */
  public void setLimit(Long count1) {
    this.limit = count1;
  }


  /**
   * record 0-indexed to start with... second page would be offset 100
   * @param offset1 the offset to set
   */
  public void setOffset(Long offset1) {
    this.offset = offset1;
  }


  /**
   * field name is dependent on the search, e.g. displayName
   * @param sortField1 the sortField to set
   */
  public void setSortField(String sortField1) {
    this.sortField = sortField1;
  }


  /**
   * @param totalCount1 the totalCount to set
   */
  public void setTotalCount(Long totalCount1) {
    this.totalCount = totalCount1;
  }
  
}
