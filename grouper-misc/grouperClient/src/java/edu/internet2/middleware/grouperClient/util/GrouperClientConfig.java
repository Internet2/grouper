/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouperClient.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingConfig;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;

/**
 * hierarchical config class for grouper.client.properties
 * @author mchyzer
 *
 */
public class GrouperClientConfig extends ConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private GrouperClientConfig() {
    
  }
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperClientConfig retrieveConfig() {
    return retrieveConfig(GrouperClientConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "grouperClient.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper.client.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper.client.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouperClient.config.secondsBetweenUpdateChecks";
  }

  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#getClassInSiblingJar
   */
  @Override
  protected Class<?> getClassInSiblingJar() {
    return GrouperClientCommonUtils.class;
  }
  
  /**
   * 
   */
  private static Log log = GrouperClientUtils.retrieveLog(GrouperClientConfig.class);
  
  /**
   * cache the messaging configs
   */
  private Map<String, GrouperMessagingConfig> grouperMessagingConfigs;

  /**
   * pattern for messaging system
   */
  private static Pattern grouperMessagingConfigPattern = Pattern.compile("^grouper.messaging.system.([^.]+).name$");
  
  /**
   * process configs for messaging and return the map 
   * @return the configs
   */
  public Map<String, GrouperMessagingConfig> retrieveGrouperMessagingConfigs() {
    if (this.grouperMessagingConfigs == null) {
      synchronized (GrouperClientConfig.class) {
        if (this.grouperMessagingConfigs == null) {
          Map<String, GrouperMessagingConfig> theGrouperMessagingConfigs = new HashMap<String, GrouperMessagingConfig>();
          
          for (String configName : this.propertyNames()) {
            
            //  # name of a messaging system.  note, "myAwsMessagingSystem" can be arbitrary
            //  # grouper.messaging.system.myAwsMessagingSystem.name = aws
            //
            //  # class that implements edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem
            //  # grouper.messaging.system.myAwsMessagingSystem.class = 

            Matcher matcher = grouperMessagingConfigPattern.matcher(configName);
            if (matcher.matches()) {
              String name = matcher.group(1);
              GrouperMessagingConfig grouperMessagingConfig = new GrouperMessagingConfig();
              grouperMessagingConfig.setName(name);
              String theClassName = this.propertyValueString("grouper.messaging.system." + name + ".class");
              try {
                Class<GrouperMessagingSystem> grouperMessagingSystemClass = GrouperClientUtils.forName(theClassName);
                
                //make sure implements interface
                if (!GrouperMessagingSystem.class.isAssignableFrom(grouperMessagingSystemClass)) {
                  throw new RuntimeException(theClassName + " class does not implement " + GrouperMessagingSystem.class.getName());
                }
                grouperMessagingConfig.setTheClass(grouperMessagingSystemClass);
                theGrouperMessagingConfigs.put(name, grouperMessagingConfig);
              } catch (Exception e) {
                log.error("Cant instantiate messaging system: " + name + ", " + theClassName, e);
              }
              
            }
          }
          
          this.grouperMessagingConfigs = theGrouperMessagingConfigs;
        }
      }
    }
    return this.grouperMessagingConfigs;
  }
  
}
