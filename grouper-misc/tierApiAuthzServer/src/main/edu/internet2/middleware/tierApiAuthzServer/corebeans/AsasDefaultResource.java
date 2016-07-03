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
package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;

/**
 * from url: BASE_URL: e.g. url/tierApiAuthz
 * default resource
 * 
 * @author mchyzer
 *
 */
public class AsasDefaultResource {

  /**
   * 
   */
  public AsasDefaultResource() {
    super();
    
    this.jsonDefaultUri = StandardApiServerUtils.servletUrl() + ".json";
    
  }


  /**
   * "jsonDefaultUri": "https://groups.institution.edu/groupsApp/tierApiAuthz.json",
   */
  private String jsonDefaultUri;

  /**
   * "jsonDefaultUri": "https://groups.institution.edu/groupsApp/tierApiAuthz.json",
   * @return the jsonDefaultUri
   */
  public String getJsonDefaultUri() {
    return this.jsonDefaultUri;
  }

  
  /**
   * "jsonDefaultUri": "https://groups.institution.edu/groupsApp/tierApiAuthz.json",
   * @param jsonDefaultUri1 the jsonDefaultUri to set
   */
  public void setJsonDefaultUri(String jsonDefaultUri1) {
    this.jsonDefaultUri = jsonDefaultUri1;
  }
  
  
  
}
