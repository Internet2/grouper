/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperActivemq.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Grouper activemq config
 */
public class GrouperActivemqConfig extends ConfigPropertiesCascadeBase {

  /**
   * subject source to login attribute name
   */
  private Map<String, String> subjectSourceToLoginAttributeName = null;
  
  /**
   * subject source map from grouper activemq config
   * @return the map
   */
  public Map<String, String> subjectSourceToLoginAttributeName() {
    if (this.subjectSourceToLoginAttributeName == null) {
     
      synchronized(this) {
        
        if (this.subjectSourceToLoginAttributeName == null) {
          
          Map<String, String> tempSubjectSourceToLoginAttributeName = new HashMap<String, String>();
          
          //grouperActivemq.subjectSource.0.sourceId = pennperson
          //grouperActivemq.subjectSource.0.subjectAttributeForLogin
          
          for (int i=0;i<100;i++) {
            if (this.containsKey("grouperActivemq.subjectSource." + i + ".sourceId")) {
              
              String sourceId = this.propertyValueString("grouperActivemq.subjectSource." + i + ".sourceId");
              String subjectAttributeForLogin = this.propertyValueStringRequired("grouperActivemq.subjectSource." + i + ".subjectAttributeForLogin");
              
              tempSubjectSourceToLoginAttributeName.put(sourceId, subjectAttributeForLogin);
              
            }
            
          }
          
          this.subjectSourceToLoginAttributeName = tempSubjectSourceToLoginAttributeName;
          
        }
        
      }
      
    }
    return this.subjectSourceToLoginAttributeName;
  }
  
  /**
   * 
   */
  public GrouperActivemqConfig() {
  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperActivemqConfig retrieveConfig() {
    return retrieveConfig(GrouperActivemqConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    this.subjectSourceToLoginAttributeName = null;
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "grouperActivemq.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper.activemq.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper.activemq.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouperActivemq.config.secondsBetweenUpdateChecks";
  }

}
