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
/**
 * 
 */
package edu.internet2.middleware.grouper.client;


/**
 * context object for client authentication, holds data about the current executing context
 * @author mchyzer
 *
 */
public class ClientCustomizerContext {
  
  /**
   * <pre>
   * name of the connection in the grouper.properties, used to get config information. 
   * 
   * e.g. for grouperClient.demo_1_6.webService.url
   * the name is demo_1_6
   * </pre>
   */
  private String connectionName;

  /**
   * <pre>
   * name of the connection in the grouper.properties, used to get config information. 
   * 
   * e.g. for grouperClient.demo_1_6.webService.url
   * the name is demo_1_6
   * </pre>
   * @return connection name
   */
  public String getConnectionName() {
    return this.connectionName;
  }

  /**
   * <pre>
   * name of the connection in the grouper.properties, used to get config information. 
   * 
   * e.g. for grouperClient.demo_1_6.webService.url
   * the name is demo_1_6
   * </pre>
   * @param grouperPropertiesConnectionName1
   */
  public void setConnectionName(String grouperPropertiesConnectionName1) {
    this.connectionName = grouperPropertiesConnectionName1;
  }
  
}
