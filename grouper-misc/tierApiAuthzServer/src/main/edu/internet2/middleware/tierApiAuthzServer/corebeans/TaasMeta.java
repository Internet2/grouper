package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerJsonIndenter;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;

/**
 * This is the meta object in a response, or extend this if 
 * @author mchyzer
 *
 */
public class TaasMeta {
    
  private String resourceType;
  
  /**
   * @return the resourceType
   */
  public String getResourceType() {
    return this.resourceType;
  }

  
  /**
   * @param resourceType the resourceType to set
   */
  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }


  /**
   * version of the API, which is the main version (largest), and dot, and the
   * build number of the release.  this is two number, i.e. 1.3 is less than 1.21
   * e.g. 1.4, this is the point in time version of the spec which is implemented
   */
  private String serverVersion;
  /**
   * points to the default resource with no formatting on the end
   * e.g. https://groups.school.edu/tierApiAuthz/tierApiAuthz
   */
  private String serviceRootUri;

  /**
   * version of the API, which is the main version (largest), and dot, and the
   * build number of the release.  this is two number, i.e. 1.3 is less than 1.21
   * e.g. 1.4, this is the point in time version of the spec which is implemented
   * @return the server version
   */
  public String getServerVersion() {
    return this.serverVersion;
  }

  /**
   * points to the default resource with no formatting on the end
   * e.g. https://groups.school.edu/tierApiAuthz/tierApiAuthz
   * @return service root uri
   */
  public String getServiceRootUri() {
    return this.serviceRootUri;
  }

  /**
   * version of the API, which is the main version (largest), and dot, and the
   * build number of the release.  this is two number, i.e. 1.3 is less than 1.21
   * e.g. 1.4, this is the point in time version of the spec which is implemented
   * @param serverVersion1
   */
  public void setServerVersion(String serverVersion1) {
    this.serverVersion = serverVersion1;
  }

  /**
   * points to the default resource with no formatting on the end
   * e.g. https://groups.school.edu/tierApiAuthz/tierApiAuthz
   * @param serviceRootUri1
   */
  public void setServiceRootUri(String serviceRootUri1) {
    this.serviceRootUri = serviceRootUri1;
  }

  /**
   * 
   */
  public TaasMeta() {
    super();
    this.setSuccess(true);
    this.setResultCode("SUCCESS");
    this.serverVersion = "1.0";
    this.serviceRootUri = StandardApiServerUtils.servletUrl();

  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    TaasMeta asacMetaResponse = new TaasMeta();
    asacMetaResponse.setLastModified("abc");
    asacMetaResponse.setSelfUri("bcd");
    
    String json = StandardApiServerUtils.jsonConvertToNoWrap(asacMetaResponse);
    
    json = new StandardApiServerJsonIndenter(json).result();
    
    System.out.println(json);
    
    asacMetaResponse = (TaasMeta)StandardApiServerUtils.jsonConvertFrom(json, TaasMeta.class);

    System.out.println(asacMetaResponse.getLastModified());
    System.out.println(asacMetaResponse.getSelfUri());
    
    json = "{\"lastModified\":\"abc\", \"selfUri\":\"bcd\", \"xyzWhatever\":\"cde\"}";

    asacMetaResponse = (TaasMeta)StandardApiServerUtils.jsonConvertFrom(json, TaasMeta.class);

    System.out.println(asacMetaResponse.getLastModified());
    System.out.println(asacMetaResponse.getSelfUri());

    
  }
  

  /** 
   * if there are warnings, they will be there
   */
  private StringBuilder resultWarning = new StringBuilder();

  /**
   * append error message to list of error messages
   * 
   * @param warning
   */
  public void appendWarning(String warning) {
    if (this.resultWarning.length() > 0) {
      this.resultWarning.append(", ");
    }
    this.resultWarning.append(warning);
  }

  /**
   * if there are warnings, they will be there
   * @return any warnings
   */
  public String getWarning() {
    return StandardApiServerUtils.trimToNull(this.resultWarning.toString());
  }

  /**
   * the builder for warnings
   * @return the builder for warnings
   */
  public StringBuilder warnings() {
    return this.resultWarning;
  }


  /**
   * @param resultWarnings1 the resultWarnings to set
   */
  public void setWarning(String resultWarnings1) {
    this.resultWarning = StandardApiServerUtils.isBlank(resultWarnings1) ? new StringBuilder() : new StringBuilder(resultWarnings1);
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
   * response status text code
   */
  private String resultCode;
  
  /**
   * response status text code
   * @return the statusCode
   */
  public String getResultCode() {
    return this.resultCode;
  }

  
  /**
   * response status text code
   * @param statusCode1 the statusCode to set
   */
  public void setResultCode(String statusCode1) {
    this.resultCode = statusCode1;
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

  private String version;
  
  
  
  
  /**
   * @return the version
   */
  public String getVersion() {
    return this.version;
  }


  
  /**
   * @param version the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }


  private String created;
  
  


  
  /**
   * @return the created
   */
  public String getCreated() {
    return this.created;
  }


  
  /**
   * @param created the created to set
   */
  public void setCreated(String created) {
    this.created = created;
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
  
  
  
  /**
   * the timestamp that this was sent from the server (at the end of the processing)
   */
  private String responseTimestamp;
  
  /**
   * freeform text about the request that the server processed, when debugging to make sure the server is processing the right params
   */
  private String requestProcessed;

  /**
   * the timestamp that this was sent from the server (at the end of the processing)
   * @return the responseTimestamp
   */
  public String getResponseTimestamp() {
    return this.responseTimestamp;
  }

  
  /**
   * the timestamp that this was sent from the server (at the end of the processing)
   * @param responseTimestamp1 the responseTimestamp to set
   */
  public void setResponseTimestamp(String responseTimestamp1) {
    this.responseTimestamp = responseTimestamp1;
  }


  /**
   * number of milliseconds that the server took in processing this request
   */
  private Long millis;
  
  /**
   * number of milliseconds that the server took in processing this request
   * @return the millis
   */
  public Long getMillis() {
    return this.millis;
  }

  /**
   * number of milliseconds that the server took in processing this request
   * @param millis1 the millis to set
   */
  public void setMillis(Long millis1) {
    this.millis = millis1;
  }

  
  /**
   * freeform text about the request that the server processed, when debugging to make sure the server is processing the right params
   * @return the requestProcessed
   */
  public String getRequestProcessed() {
    return this.requestProcessed;
  }

  
  /**
   * freeform text about the request that the server processed, when debugging to make sure the server is processing the right params
   * @param requestProcessed1 the requestProcessed to set
   */
  public void setRequestProcessed(String requestProcessed1) {
    this.requestProcessed = requestProcessed1;
  }

  /**
   * number of the HTTP httpStatusCode code
   */
  private Integer httpStatusCode;
  

  /**
   * number of the HTTP httpStatusCode code
   * @return the httpgetHttpStatusCodee
   */
  public Integer getHttpStatusCode() {
    return this.httpStatusCode;
  }

  
  /**
   * number of the HTTP httpStatusCode code
   *setHttpStatusCodetatus1 the httpStatusCode to set
   */
  public void setHttpStatusCode(Integer status1) {
    this.httpStatusCode = status1;
  }

  
}
