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
   * @param clientAuthenticationContext
   */
  public void setupConnection() {
    
    //this is a threadlocal override map for the grouper client
    Map<String, String> overrideMap = GrouperClientUtils.propertiesThreadLocalOverrideMap("grouper.client.properties");
    overrideMap.clear();
    String connectionName = this.clientCustomizerContext.getConnectionName();
    
    String grouperPropertiesPrefix = "grouperClient." + connectionName + ".properties";
    
    //loop through properties in the grouper.properties and see which to move over to the client config
    for (String propertyName : GrouperConfig.getPropertyNames()) {
      if (propertyName.startsWith(grouperPropertiesPrefix)) {

        //grouper properties value
        String value = GrouperConfig.getProperty(propertyName);
        
        //e.g. grouperClient.localhost.properties.grouperClient.webService.url
        //get the part after the prefix
        String clientPropertyName = propertyName.substring(grouperPropertiesPrefix.length()+1, propertyName.length());
        
        overrideMap.put(clientPropertyName, value);
        
      }
    }
    
  }

  /**
   * when connection is done
   * @param clientAuthenticationContext
   */
  public void teardownConnection() {
    Map<String, String> overrideMap = GrouperClientUtils.propertiesThreadLocalOverrideMap("grouper.client.properties");
    overrideMap.clear();
  }

}
