/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
package edu.internet2.middleware.tierInstrumentationCollector.corebeans;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.tierInstrumentationCollector.util.TierInstrumentationCollectorUtils;

/**
 * This is the meta object in a response, or extend this if 
 * @author mchyzer
 *
 */
public class TicMeta {
    
  /**
   * tier error message: If this is an error this can hold the free form error message
   */
  private String tierErrorMessage;
  
  
  /**
   * tier error message: If this is an error this can hold the free form error message
   * @return the tierErrorMessage
   */
  public String getTierErrorMessage() {
    return this.tierErrorMessage;
  }

  /**
   * 
   */
  private String tierRequestId;
  
  /**
   * @return the tierRequestId
   */
  public String getTierRequestId() {
    return this.tierRequestId;
  }
  
  /**
   * @param tierRequestId1 the tierRequestId to set
   */
  public void setTierRequestId(String tierRequestId1) {
    this.tierRequestId = tierRequestId1;
  }


  /**
   * tier error message: If this is an error this can hold the free form error message
   * @param tierErrorMessage1 the tierErrorMessage to set
   */
  public void setTierErrorMessage(String tierErrorMessage1) {
    this.tierErrorMessage = tierErrorMessage1;
  }


  /**
   * The name of the resource type of the resource. This
   * attribute has a mutability of "readOnly" and "caseExact" as
   * "true".
   */
  private String resourceType;
  
  /**
   * @return the resourceType
   */
  public String getResourceType() {
    return this.resourceType;
  }

  
  /**
   * @param resourceType1 the resourceType to set
   */
  public void setResourceType(String resourceType1) {
    this.resourceType = resourceType1;
  }


  /**
   * version of the API, which is the main version (largest), and dot, and the
   * build number of the release.  this is two number, i.e. 1.3 is less than 1.21
   * e.g. 1.4, this is the point in time version of the spec which is implemented
   */
  private String tierServerVersion;
  /**
   * points to the default resource with no formatting on the end
   * e.g. https://groups.school.edu/tierApiAuthz/tierApiAuthz
   */
  private String tierServiceRootUri;

  /**
   * version of the API, which is the main version (largest), and dot, and the
   * build number of the release.  this is two number, i.e. 1.3 is less than 1.21
   * e.g. 1.4, this is the point in time version of the spec which is implemented
   * @return the server version
   */
  public String getTierServerVersion() {
    return this.tierServerVersion;
  }

  /**
   * points to the default resource with no formatting on the end
   * e.g. https://groups.school.edu/tierApiAuthz/tierApiAuthz
   * @return service root uri
   */
  public String getTierServiceRootUri() {
    return this.tierServiceRootUri;
  }

  /**
   * version of the API, which is the main version (largest), and dot, and the
   * build number of the release.  this is two number, i.e. 1.3 is less than 1.21
   * e.g. 1.4, this is the point in time version of the spec which is implemented
   * @param serverVersion1
   */
  public void setTierServerVersion(String serverVersion1) {
    this.tierServerVersion = serverVersion1;
  }

  /**
   * points to the default resource with no formatting on the end
   * e.g. https://groups.school.edu/tierApiAuthz/tierApiAuthz
   * @param serviceRootUri1
   */
  public void setTierServiceRootUri(String serviceRootUri1) {
    this.tierServiceRootUri = serviceRootUri1;
  }

  /**
   * 
   */
  public TicMeta() {
    super();
    this.setTierSuccess(true);
    this.setTierResultCode("SUCCESS");
    this.tierServerVersion = "1.0";
    this.tierServiceRootUri = TierInstrumentationCollectorUtils.servletUrl();

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
  public String getTierWarning() {
    return StringUtils.trimToNull(this.resultWarning.toString());
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
    this.resultWarning = StringUtils.isBlank(resultWarnings1) ? new StringBuilder() : new StringBuilder(resultWarnings1);
  }


//  /** with paging, retrieve records after this value */
//  private String offsetFieldValue;
//
//  /**
//   * with paging, retrieve records after this value
//   * @return the offsetFieldValue
//   */
//  public String getOffsetFieldValue() {
//    return this.offsetFieldValue;
//  }
//  
//  /**
//   * with paging, retrieve records after this value
//   * @param offsetFieldValue1 the offsetFieldValue to set
//   */
//  public void setOffsetFieldValue(String offsetFieldValue1) {
//    this.offsetFieldValue = offsetFieldValue1;
//  }


  /**
   * response status text code
   */
  private String tierResultCode;
  
  /**
   * response status text code
   * @return the statusCode
   */
  public String getTierResultCode() {
    return this.tierResultCode;
  }

  
  /**
   * response status text code
   * @param statusCode1 the statusCode to set
   */
  public void setTierResultCode(String statusCode1) {
    this.tierResultCode = statusCode1;
  }

  /**
   * true or false if valid request
   */
  private Boolean tierSuccess;
  
  /**
   * true or false if valid request
   * @return the success
   */
  public Boolean getTierSuccess() {
    return this.tierSuccess;
  }

  
  /**
   * true or false if valid request
   * @param success1 the success to set
   */
  public void setTierSuccess(Boolean success1) {
    this.tierSuccess = success1;
  }

  /**
   * The version of the resource being returned. This value
   * must be the same as the entity-tag (ETag) HTTP response header
   * (see Sections 2.1 and 2.3 of [RFC7232]). This attribute has
   * "caseExact" as "true". Service provider support for this
   * attribute is optional and subject to the service provider's
   * support for versioning (see Section 3.14 of [RFC7644]). If a
   * service provider provides "version" (entity-tag) for a
   * representation and the generation of that entity-tag does not
   * satisfy all of the characteristics of a strong validator (see
   * Section 2.1 of [RFC7232]), then the origin server MUST mark the
   * "version" (entity-tag) as weak by prefixing its opaque value
   * with "W/" (case sensitive).
   */
  private String version;
  
  
  
  
  /**
   * The version of the resource being returned. This value
   * must be the same as the entity-tag (ETag) HTTP response header
   * (see Sections 2.1 and 2.3 of [RFC7232]). This attribute has
   * "caseExact" as "true". Service provider support for this
   * attribute is optional and subject to the service provider's
   * support for versioning (see Section 3.14 of [RFC7644]). If a
   * service provider provides "version" (entity-tag) for a
   * representation and the generation of that entity-tag does not
   * satisfy all of the characteristics of a strong validator (see
   * Section 2.1 of [RFC7232]), then the origin server MUST mark the
   * "version" (entity-tag) as weak by prefixing its opaque value
   * with "W/" (case sensitive).
   * @return the version
   */
  public String getVersion() {
    return this.version;
  }


  
  /**
   * The version of the resource being returned. This value
   * must be the same as the entity-tag (ETag) HTTP response header
   * (see Sections 2.1 and 2.3 of [RFC7232]). This attribute has
   * "caseExact" as "true". Service provider support for this
   * attribute is optional and subject to the service provider's
   * support for versioning (see Section 3.14 of [RFC7644]). If a
   * service provider provides "version" (entity-tag) for a
   * representation and the generation of that entity-tag does not
   * satisfy all of the characteristics of a strong validator (see
   * Section 2.1 of [RFC7232]), then the origin server MUST mark the
   * "version" (entity-tag) as weak by prefixing its opaque value
   * with "W/" (case sensitive).
   * @param version1 the version to set
   */
  public void setVersion(String version1) {
    this.version = version1;
  }

  /**
   * The "DateTime" that the resource was added to the service provider
   */
  private String created;
  
  /**
   * The "DateTime" that the resource was added to the service provider
   * @return the created
   */
  public String getCreated() {
    return this.created;
  }

  /**
   * The "DateTime" that the resource was added to the service provider
   * @param created1 the created to set
   */
  public void setCreated(String created1) {
    this.created = created1;
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
  private String location;
  
  /**
   * Self URI to this resource 
   * e.g. https://groups.institution.edu/groupsApp/groupsApi 
   * @return the selfUri
   */
  public String getLocation() {
    return this.location;
  }

  
  /**
   * Self URI to this resource 
   * e.g. https://groups.institution.edu/groupsApp/groupsApi 
   * @param selfUri1 the selfUri to set
   */
  public void setLocation(String selfUri1) {
    this.location = selfUri1;
  }
  
//  /** true or false sorting ascending or descending */
//  private Boolean ascending;
//
//  /** number of records that are being returned */
//  private Long limit;
//
//  /** record 0-indexed to start with... second page would be offset 100 */
//  private Long offset;
//
//  /** field name is dependent on the search, e.g. displayName */
//  private String sortField;
//
//  /** total number of records (not just the ones being returned, but overall) */
//  private Long totalCount;
//
//
//  /**
//   * true or false sorting ascending or descending
//   * @return the ascending
//   */
//  public Boolean getAscending() {
//    return this.ascending;
//  }
//
//
//  /**
//   * number of records that are being returned
//   * @return the limit
//   */
//  public Long getLimit() {
//    return this.limit;
//  }
//
//
//  /**
//   * record 0-indexed to start with... second page would be offset 100
//   * @return the offset
//   */
//  public Long getOffset() {
//    return this.offset;
//  }
//
//
//  /**
//   * field name is dependent on the search, e.g. displayName
//   * @return the sortField
//   */
//  public String getSortField() {
//    return this.sortField;
//  }
//
//
//  /**
//   * total number of records (not just the ones being returned, but overall)
//   * @return the totalCount
//   */
//  public Long getTotalCount() {
//    return this.totalCount;
//  }
//
//
//  /**
//   * true or false sorting ascending or descending
//   * @param ascending1 the ascending to set
//   */
//  public void setAscending(Boolean ascending1) {
//    this.ascending = ascending1;
//  }
//
//
//  /**
//   * number of records that are being returned
//   * @param count1 the limit to set
//   */
//  public void setLimit(Long count1) {
//    this.limit = count1;
//  }
//
//
//  /**
//   * record 0-indexed to start with... second page would be offset 100
//   * @param offset1 the offset to set
//   */
//  public void setOffset(Long offset1) {
//    this.offset = offset1;
//  }
//
//
//  /**
//   * field name is dependent on the search, e.g. displayName
//   * @param sortField1 the sortField to set
//   */
//  public void setSortField(String sortField1) {
//    this.sortField = sortField1;
//  }
//
//
//  /**
//   * @param totalCount1 the totalCount to set
//   */
//  public void setTotalCount(Long totalCount1) {
//    this.totalCount = totalCount1;
//  }
//  
  
  
  /**
   * freeform text about the request that the server processed, when debugging to make sure the server is processing the right params
   */
  private String tierDebugMessage;

  /**
   * number of milliseconds that the server took in processing this request
   */
  private Long tierResponseDurationMillis;
  
  /**
   * number of milliseconds that the server took in processing this request
   * @return the millis
   */
 public Long getTierResponseDurationMillis() {
    return this.tierResponseDurationMillis;
  }

  /**
   * number of milliseconds that the server took in processing this request
   * @param millis1 the millis to set
   */
  public void setTierResponseDurationMillis(Long millis1) {
    this.tierResponseDurationMillis = millis1;
  }

  
  /**
   * freeform text about the request that the server processed, when debugging to make sure the server is processing the right params
   * @return the requestProcessed
   */
  public String getTierDebugMessage() {
    return this.tierDebugMessage;
  }

  
  /**
   * freeform text about the request that the server processed, when debugging to make sure the server is processing the right params
   * @param requestProcessed1 the requestProcessed to set
   */
  public void setTierDebugMessage(String requestProcessed1) {
    this.tierDebugMessage = requestProcessed1;
  }

  /**
   * number of the HTTP httpStatusCode code
   */
  private Integer tierHttpStatusCode;
  

  /**
   * number of the HTTP httpStatusCode code
   * @return the httpgetHttpStatusCodee
   */
  public Integer getTierHttpStatusCode() {
    return this.tierHttpStatusCode;
  }

  /**
   * override this for subject objects to scimify
   * make this a scim response
   */
  public void scimify() {
    this.resultWarning.setLength(0);
    this.setTierDebugMessage(null);
    this.setTierErrorMessage(null);
    this.setTierHttpStatusCode(null);
    this.setTierRequestId(null);
    this.setTierResponseDurationMillis(null);
    this.setTierResultCode(null);
    this.setTierServerVersion(null);
    this.setTierServiceRootUri(null);
    this.setTierSuccess(null);
  }

  /**
   * number of the HTTP httpStatusCode code
   * @param status1 the httpStatusCode to set
   */
  public void setTierHttpStatusCode(Integer status1) {
    this.tierHttpStatusCode = status1;
  }

  
}
