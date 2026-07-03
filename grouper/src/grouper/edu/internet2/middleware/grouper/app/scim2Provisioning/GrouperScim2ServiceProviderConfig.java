package edu.internet2.middleware.grouper.app.scim2Provisioning;

/**
 * The capabilities a SCIM target advertises at GET /ServiceProviderConfig (patch, bulk, sort,
 * filter with its maxResults, etag, changePassword).  Used by provisioning diagnostics to validate
 * the provisioner configuration against what the target actually supports.  A null field means the
 * target did not advertise that capability.
 */
public class GrouperScim2ServiceProviderConfig {

  private Boolean patchSupported;
  private Boolean bulkSupported;
  private Boolean sortSupported;
  private Boolean filterSupported;
  private Integer filterMaxResults;
  private Boolean etagSupported;
  private Boolean changePasswordSupported;

  public Boolean getPatchSupported() {
    return patchSupported;
  }

  public void setPatchSupported(Boolean patchSupported) {
    this.patchSupported = patchSupported;
  }

  public Boolean getBulkSupported() {
    return bulkSupported;
  }

  public void setBulkSupported(Boolean bulkSupported) {
    this.bulkSupported = bulkSupported;
  }

  public Boolean getSortSupported() {
    return sortSupported;
  }

  public void setSortSupported(Boolean sortSupported) {
    this.sortSupported = sortSupported;
  }

  public Boolean getFilterSupported() {
    return filterSupported;
  }

  public void setFilterSupported(Boolean filterSupported) {
    this.filterSupported = filterSupported;
  }

  public Integer getFilterMaxResults() {
    return filterMaxResults;
  }

  public void setFilterMaxResults(Integer filterMaxResults) {
    this.filterMaxResults = filterMaxResults;
  }

  public Boolean getEtagSupported() {
    return etagSupported;
  }

  public void setEtagSupported(Boolean etagSupported) {
    this.etagSupported = etagSupported;
  }

  public Boolean getChangePasswordSupported() {
    return changePasswordSupported;
  }

  public void setChangePasswordSupported(Boolean changePasswordSupported) {
    this.changePasswordSupported = changePasswordSupported;
  }

  @Override
  public String toString() {
    return "GrouperScim2ServiceProviderConfig[patch=" + patchSupported + ", bulk=" + bulkSupported
        + ", sort=" + sortSupported + ", filter=" + filterSupported + ", filterMaxResults=" + filterMaxResults
        + ", etag=" + etagSupported + ", changePassword=" + changePasswordSupported + "]";
  }
}
