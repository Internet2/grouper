/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: AsacApiDefaultResource.java,v 1.7 2009-12-13 06:33:06 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiClient.api;

import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacDefaultVersionResourceContainer;
import edu.internet2.middleware.authzStandardApiClient.ws.AsacRestHttpMethod;
import edu.internet2.middleware.authzStandardApiClient.ws.StandardApiClientWs;



/**
 * class to run an add member web service call
 */
public class AsacApiDefaultVersionResource extends AsacApiRequestBase {

  /**
   * @see edu.internet2.middleware.authzStandardApiClient.api.AsacApiRequestBase#assignIndent(boolean)
   */
  @Override
  public AsacApiDefaultVersionResource assignIndent(boolean indent1) {
    return (AsacApiDefaultVersionResource)super.assignIndent(indent1);
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
  public AsacDefaultVersionResourceContainer execute() {
    this.validate();
    AsacDefaultVersionResourceContainer asacDefaultVersionResourceContainer = null;
      
    StandardApiClientWs<AsacDefaultVersionResourceContainer> standardApiClientWs = new StandardApiClientWs<AsacDefaultVersionResourceContainer>();
    
    //kick off the web service
    //MCH dont fill all this in since could be uuid based
    String urlSuffix = "." + this.getContentType().name();
    asacDefaultVersionResourceContainer =
      standardApiClientWs.executeService(urlSuffix, null, "defaultVersionResource", null,
          this.getContentType(), AsacDefaultVersionResourceContainer.class, AsacRestHttpMethod.GET);
    
    return asacDefaultVersionResourceContainer;
    
  }
  
}
