package edu.internet2.middleware.authzStandardApiServer.corebeans;

import java.util.Date;

import edu.internet2.middleware.authzStandardApiServer.j2ee.AsasRestServlet;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;

/**
 * default resource
 * 
 * @author mchyzer
 *
 */
public class AsasDefaultResource extends AsasResponseBeanBase {
  
  public AsasDefaultResource() {
    super();
    this.get_requestMeta().setHttpStatusCode(200);
    this.get_meta().setLastModified(StandardApiServerUtils.convertToIso8601(new Date(AsasRestServlet.getStartupTime())));
    this.setSuccess(true);
    
    this.jsonDefaultUri = StandardApiServerUtils.servletUrl() + ".json";
    
    this.xmlDefaultUri = StandardApiServerUtils.servletUrl() + ".xml";
    
  }


  /**
   * "jsonDefaultUri": "https://groups.institution.edu/groupsApp/authzStandardApi.json",
   */
  private String jsonDefaultUri;

  /**
   * "xmlDefaultUri": "https://groups.institution.edu/groupsApp/authzStandardApi.xml"
   */
  private String xmlDefaultUri;

  
  /**
   * "jsonDefaultUri": "https://groups.institution.edu/groupsApp/authzStandardApi.json",
   * @return the jsonDefaultUri
   */
  public String getJsonDefaultUri() {
    return this.jsonDefaultUri;
  }

  
  /**
   * "jsonDefaultUri": "https://groups.institution.edu/groupsApp/authzStandardApi.json",
   * @param jsonDefaultUri1 the jsonDefaultUri to set
   */
  public void setJsonDefaultUri(String jsonDefaultUri1) {
    this.jsonDefaultUri = jsonDefaultUri1;
  }

  
  /**
   * "xmlDefaultUri": "https://groups.institution.edu/groupsApp/authzStandardApi.xml"
   * @return the xmlDefaultUri
   */
  public String getXmlDefaultUri() {
    return this.xmlDefaultUri;
  }

  
  /**
   * "xmlDefaultUri": "https://groups.institution.edu/groupsApp/authzStandardApi.xml"
   * @param xmlDefaultUri1 the xmlDefaultUri to set
   */
  public void setXmlDefaultUri(String xmlDefaultUri1) {
    this.xmlDefaultUri = xmlDefaultUri1;
  }
  
  
  
}
