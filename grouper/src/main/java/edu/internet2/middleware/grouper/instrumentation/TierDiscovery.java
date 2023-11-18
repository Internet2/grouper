/**
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
 */
package edu.internet2.middleware.grouper.instrumentation;

import java.util.Map;

/**
 * @author shilen
 */
public class TierDiscovery {

  private boolean serviceEnabled;
  
  private Map<String, String>[] endpoints;

  
  /**
   * @return the serviceEnabled
   */
  public boolean isServiceEnabled() {
    return serviceEnabled;
  }

  
  /**
   * @param serviceEnabled the serviceEnabled to set
   */
  public void setServiceEnabled(boolean serviceEnabled) {
    this.serviceEnabled = serviceEnabled;
  }

  
  /**
   * @return the endpoints
   */
  public Map<String, String>[] getEndpoints() {
    return endpoints;
  }

  
  /**
   * @param endpoints the endpoints to set
   */
  public void setEndpoints(Map<String, String>[] endpoints) {
    this.endpoints = endpoints;
  }

}
