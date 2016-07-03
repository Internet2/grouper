/*
 * @author mchyzer
 * $Id: AsacApiDefaultResource.java,v 1.7 2009-12-13 06:33:06 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzClient.api;

import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacDefaultResourceContainer;
import edu.internet2.middleware.tierApiAuthzClient.ws.AsacRestHttpMethod;
import edu.internet2.middleware.tierApiAuthzClient.ws.StandardApiClientWs;



/**
 * class to run an add member web service call
 */
public class AsacApiDefaultResource extends AsacApiRequestBase {

  /**
   * @see edu.internet2.middleware.tierApiAuthzClient.api.AsacApiRequestBase#assignIndent(boolean)
   */
  @Override
  public AsacApiDefaultResource assignIndent(boolean indent1) {
    return (AsacApiDefaultResource)super.assignIndent(indent1);
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
  public AsacDefaultResourceContainer execute() {
    this.validate();
    AsacDefaultResourceContainer asacDefaultResourceContainer = null;
      
    StandardApiClientWs<AsacDefaultResourceContainer> standardApiClientWs = new StandardApiClientWs<AsacDefaultResourceContainer>();
    
    //kick off the web service
    //MCH dont fill all this in since could be uuid based
    String urlSuffix = null;
    asacDefaultResourceContainer =
      standardApiClientWs.executeService(urlSuffix, null, "defaultResource", null,
          this.getContentType(), AsacDefaultResourceContainer.class, AsacRestHttpMethod.GET);
    
    return asacDefaultResourceContainer;
    
  }
  
}
