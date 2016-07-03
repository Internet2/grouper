/*
 * @author mchyzer
 * $Id: AsacApiDefaultResource.java,v 1.7 2009-12-13 06:33:06 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzClient.api;

import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacVersionResourceContainer;
import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientUtils;
import edu.internet2.middleware.tierApiAuthzClient.ws.AsacRestHttpMethod;
import edu.internet2.middleware.tierApiAuthzClient.ws.StandardApiClientWs;



/**
 * class to run an add member web service call
 */
public class AsacApiVersionResource extends AsacApiRequestBase {

  
  
  /**
   * @see edu.internet2.middleware.tierApiAuthzClient.api.AsacApiRequestBase#assignIndent(boolean)
   */
  @Override
  public AsacApiVersionResource assignIndent(boolean indent1) {
    return (AsacApiVersionResource)super.assignIndent(indent1);
  }

  /**
   * validate this call
   */
  private void validate() {
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public AsacVersionResourceContainer execute() {
    this.validate();
    AsacVersionResourceContainer asacDefaultVersionResourceContainer = null;
      
    StandardApiClientWs<AsacVersionResourceContainer> standardApiClientWs 
      = new StandardApiClientWs<AsacVersionResourceContainer>();
    
    //kick off the web service
    //MCH dont fill all this in since could be uuid based
    String urlSuffix = "/" + StandardApiClientUtils.version() 
        + "." + this.getContentType().name();

    asacDefaultVersionResourceContainer =
      standardApiClientWs.executeService(urlSuffix, null, "versionResource", null,
          this.getContentType(), AsacVersionResourceContainer.class, AsacRestHttpMethod.GET);
    
    return asacDefaultVersionResourceContainer;
    
  }
  
}
