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
/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerConfig;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


/**
 * e.g. from URL: BASE_URL.json, e.g. url/tierApiAuthz.json
 * @author mchyzer
 *
 */
public class AsasDefaultVersionResourceContainer extends AsasResponseBeanBase {

  /**
   * 
   */
  public AsasDefaultVersionResourceContainer() {
    
    this.v1Uri = "/" 
        + StandardApiServerUtils.version();
    this.serverType = StandardApiServerConfig.retrieveConfig()
        .propertyValueStringRequired("tierApiAuthzServer.serverType");
    
    if (StandardApiServerUtils.isBlank(this.serverType)) {
      throw new RuntimeException("Why is tierApiAuthzServer.serverType not defined in the the standardapi.server.properties");
    }
    
  }


  /**
   * describes the implementation of the API, i.e. the underlying groups service
   * e.g. Grouper WS Standard API v2.1.14
   */
  private String serverType;
  
  /**
   * describes the implementation of the API, i.e. the underlying groups service
   * e.g. Grouper WS Standard API v2.1.14
   * @return the server type
   */
  public String getServerType() {
    return this.serverType;
  }
  
  /**
   * describes the implementation of the API, i.e. the underlying groups service
   * e.g. Grouper WS Standard API v2.1.14
   * @param serverType
   */
  public void setServerType(String serverType) {
    this.serverType = serverType;
  }



  /**
   * "v1Uri": "https://groups.institution.edu/groupsApp/tierApiAuthz/v1.json",
   */
  private String v1Uri;


  
  /**
   * @return the v1Uri
   */
  public String getV1Uri() {
    return this.v1Uri;
  }


  
  /**
   * @param v1Uri1 the v1Uri to set
   */
  public void setV1Uri(String v1Uri1) {
    this.v1Uri = v1Uri1;
  }
  
  

}
