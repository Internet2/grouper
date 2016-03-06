package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;

/**
 * 
 * @author mchyzer
 *
 */
public class AsasServiceMeta {
  
  /**
   * 
   */
  public AsasServiceMeta() {
    
    this.serverVersion = "1.0";

    this.serviceRootUri = StandardApiServerUtils.servletUrl();

  }

  
  /**
   * version of the API, which is the main version (largest), and dot, and the
   * build number of the release.  this is two number, i.e. 1.3 is less than 1.21
   * e.g. 1.4, this is the point in time version of the spec which is implemented
   */
  private String serverVersion;
  /**
   * points to the default resource with no formatting on the end
   * e.g. https://groups.school.edu/authzStandardApi/authzStandardApi
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
   * e.g. https://groups.school.edu/authzStandardApi/authzStandardApi
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
   * e.g. https://groups.school.edu/authzStandardApi/authzStandardApi
   * @param serviceRootUri1
   */
  public void setServiceRootUri(String serviceRootUri1) {
    this.serviceRootUri = serviceRootUri1;
  }

}
