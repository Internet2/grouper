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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.ldaptive.ResultCode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class LdapConfiguration {
  
  private static Map<String, LdapConfiguration> configs = new ConcurrentHashMap<String, LdapConfiguration>();

  private boolean isActiveDirectory;
  private String dnAttributeForSearches;
  private int queryBatchSize;
  private int updateBatchSize;
  private Integer pageSize;
  private Set<ResultCode> searchIgnoreResultCodes = new HashSet<ResultCode>();
  
  /**
   * @param ldapServerId
   */
  public LdapConfiguration(String ldapServerId) {
    this.isActiveDirectory = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("ldap." + ldapServerId + ".isActiveDirectory", false);
    this.dnAttributeForSearches = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".dnAttributeForSearches", null);
    this.queryBatchSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + ldapServerId + ".queryBatchSize", 100);
    this.updateBatchSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + ldapServerId + ".updateBatchSize", 100);
    this.pageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + ldapServerId + ".pagedResultsSize");
  
    if (this.dnAttributeForSearches == null && this.isActiveDirectory) {
      this.dnAttributeForSearches = "distinguishedName";
    }
    
    if (this.queryBatchSize < 1) {
      this.queryBatchSize = 1;
    }
    
    if (this.updateBatchSize < 1) {
      this.updateBatchSize = 1;
    }
    
    String searchIgnoreResultCodesString = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".searchIgnoreResultCodes", "TIME_LIMIT_EXCEEDED, SIZE_LIMIT_EXCEEDED, PARTIAL_RESULTS");
    if (!StringUtils.isBlank(searchIgnoreResultCodesString)) {
      for (String resultCodeString : GrouperUtil.splitTrimToSet(searchIgnoreResultCodesString, ",")) {
        ResultCode resultCode = ResultCode.valueOf(resultCodeString);
        searchIgnoreResultCodes.add(resultCode);
      }
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
   * @param ldapServerId
   */
  public static void removeConfig(String ldapServerId) {
    synchronized (LdapConfiguration.class) {
      configs.remove(ldapServerId);
    }
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

  /**
   * @return batch size for updates
   */
  public int getUpdateBatchSize() {
    return updateBatchSize;
  }

  /**
   * @return page size
   */
  public Integer getPageSize() {
    return pageSize;
  }

  
  /**
   * @param pageSize
   */
  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  
  public Set<ResultCode> getSearchIgnoreResultCodes() {
    return searchIgnoreResultCodes;
  }
}
