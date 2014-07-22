/**
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
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.client;

import java.util.Map;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * extend this class to customize how authentication works
 * generally these methods will set threadlocal propreties to simulate a config file...
 */
public class ClientCustomizer {

  /** context of this customizer instance */
  private ClientCustomizerContext clientCustomizerContext;
  
  /**
   * client customizer context
   * @param theClientCustomizerContext
   */
  public void init(ClientCustomizerContext theClientCustomizerContext) {
    this.clientCustomizerContext = theClientCustomizerContext;
  }
  
  
  /**
   * when connection is setup
   */
  public void setupConnection() {
    
    //this is a threadlocal override map for the grouper client
    Map<String, String> overrideMap = GrouperClientUtils.propertiesThreadLocalOverrideMap("grouper.client.properties");
    overrideMap.clear();
    String connectionName = this.clientCustomizerContext.getConnectionName();
    
    String grouperPropertiesPrefix = "grouperClient." + connectionName + ".properties";
    
    //loop through properties in the grouper.properties and see which to move over to the client config
    for (String propertyName : GrouperConfig.retrieveConfig().propertyNames()) {
      if (propertyName.startsWith(grouperPropertiesPrefix)) {

        //grouper properties value
        String value = GrouperConfig.retrieveConfig().propertyValueString(propertyName);
        
        //e.g. grouperClient.localhost.properties.grouperClient.webService.url
        //get the part after the prefix
        String clientPropertyName = propertyName.substring(grouperPropertiesPrefix.length()+1, propertyName.length());
        
        overrideMap.put(clientPropertyName, value);
        
      }
    }
    
  }

  /**
   * when connection is done
   */
  public void teardownConnection() {
    Map<String, String> overrideMap = GrouperClientUtils.propertiesThreadLocalOverrideMap("grouper.client.properties");
    overrideMap.clear();
  }

}
