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

/**
 * from url: BASE_URL: e.g. url/tierApiAuthz
 * asas default resource container
 * @author mchyzer
 *
 */
public class AsasDefaultResourceContainer extends AsasResponseBeanBase {

  /**
   * from url: BASE_URL: e.g. url/tierApiAuthz
   * body of the asas default resource
   */
  private AsasDefaultResource defaultResource = new AsasDefaultResource();

  /**
   * from url: BASE_URL: e.g. url/tierApiAuthz
   * body of the asas default resource
   * @return the body of the response
   */
  public AsasDefaultResource getDefaultResource() {
    return this.defaultResource;
  }

  /**
   * from url: BASE_URL: e.g. url/tierApiAuthz
   * body of the asas default resource
   * @param defaultResource1
   */
  public void setDefaultResource(AsasDefaultResource defaultResource1) {
    this.defaultResource = defaultResource1;
  }
  
}
