/**
 * Copyright 2020 Internet2
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
package edu.internet2.middleware.grouper.ldap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;

/**
 * @author shilen
 */
public class LdapConfiguration {
  
  private static Map<String, LdapConfiguration> configs = new ConcurrentHashMap<String, LdapConfiguration>();

  private boolean isActiveDirectory;
  private String dnAttributeForSearches;
  private int queryBatchSize;
  
  /**
   * @param ldapServerId
   */
  public LdapConfiguration(String ldapServerId) {
    this.isActiveDirectory = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("ldap." + ldapServerId + ".isActiveDirectory", false);
    this.dnAttributeForSearches = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".dnAttributeForSearches", null);
    this.queryBatchSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + ldapServerId + ".queryBatchSize", 100);
  
    if (this.dnAttributeForSearches == null && this.isActiveDirectory) {
      this.dnAttributeForSearches = "distinguishedName";
    }
    
    if (this.queryBatchSize < 1) {
      this.queryBatchSize = 1;
    }
  }
  
  /**
   * @param ldapServerId
   * @return instance of ldap configuration class
   */
  public static LdapConfiguration getConfig(String ldapServerId) {
    if (configs.get(ldapServerId) == null) {
      synchronized (LdapConfiguration.class) {
        if (configs.get(ldapServerId) == null) {
          LdapConfiguration config = new LdapConfiguration(ldapServerId);
          configs.put(ldapServerId, config);
        }
      }
    }
    
    return configs.get(ldapServerId);
  }

  
  /**
   * @return true if config says this is an AD
   */
  public boolean isActiveDirectory() {
    return isActiveDirectory;
  }

  /**
   * @return the attribute used to search a dn, e.g. entryDn, distinguishedName, etc
   */
  public String getDnAttributeForSearches() {
    return dnAttributeForSearches;
  }

  /**
   * @return batch size for queries, e.g. (|(attr=val1)(attr=val2)(attr=val3))
   */
  public int getQueryBatchSize() {
    return queryBatchSize;
  }
}
