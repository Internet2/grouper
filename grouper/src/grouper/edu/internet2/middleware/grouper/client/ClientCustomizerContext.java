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
