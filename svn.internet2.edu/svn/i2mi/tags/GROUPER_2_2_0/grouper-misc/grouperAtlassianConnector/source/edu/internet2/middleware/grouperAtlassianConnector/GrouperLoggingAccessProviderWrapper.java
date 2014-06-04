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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.opensymphony.user.Entity.Accessor;
import com.opensymphony.user.provider.AccessProvider;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * delegate to jiraOFBizAccessProvider
 */
public class GrouperLoggingAccessProviderWrapper implements AccessProvider {

  /** temporary delegate */
  private AccessProvider accessProvider = null;
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  public GrouperLoggingAccessProviderWrapper() {
    
    String accessProviderClassName = GrouperClientUtils.propertiesValue("atlassian.logging.accessProvider.class", true);
    Class<AccessProvider> accessProviderClass = GrouperClientUtils.forName(accessProviderClassName);
    this.accessProvider = GrouperClientUtils.newInstance(accessProviderClass);
    
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperLoggingAccessProviderWrapper.class);

  /**
   * @see com.opensymphony.user.provider.AccessProvider#addToGroup(java.lang.String, java.lang.String)
   */
  @Override
  public boolean addToGroup(String username, String groupname) {
    Boolean result = null;
    
    try {
      result = this.accessProvider.addToGroup(username, groupname);
    } finally {
      LOG.info("Adding to group: " + groupname + ", username: " + username + ", result: " + result);
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.AccessProvider#inGroup(java.lang.String, java.lang.String)
   */
  @Override
  public boolean inGroup(String username, String groupname) {
    Boolean result = null;
    try {
      result = this.accessProvider.inGroup(username, groupname);
    } finally {
      LOG.info("In group: " + groupname + ", username: " + username + ", result: " + result);
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.AccessProvider#listGroupsContainingUser(java.lang.String)
   */
  @Override
  public List<String> listGroupsContainingUser(String username) {
    List<String> result = null;
    try {
      result = this.accessProvider.listGroupsContainingUser(username);
    } finally {
      StringBuilder logMessage = new StringBuilder("listGroupsContainingUser for username: " + username +": ");
      if (result == null) {
        logMessage.append("null");
      } else {
        for (int i=0;i<result.size();i++) {
          logMessage.append(result.get(i));
          if (i < result.size()) {
            logMessage.append(", ");
          }
        }
      }
      LOG.info(logMessage.toString());
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.AccessProvider#listUsersInGroup(java.lang.String)
   */
  @Override
  public List<String> listUsersInGroup(String groupname) {
    List<String> result = null;
    try {
      result = this.accessProvider.listUsersInGroup(groupname);
    } finally {
      StringBuilder logMessage = new StringBuilder("listUsersInGroup for groupname: " + groupname +": ");
      if (result == null) {
        logMessage.append("null");
      } else {
        for (int i=0;i<result.size();i++) {
          logMessage.append(result.get(i));
          if (i < result.size()) {
            logMessage.append(", ");
          }
        }
      }
      LOG.info(logMessage.toString());
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.AccessProvider#removeFromGroup(java.lang.String, java.lang.String)
   */
  @Override
  public boolean removeFromGroup(String username, String groupname) {
    Boolean result = null;
    try {
      result = this.accessProvider.removeFromGroup(username, groupname);
    } finally {
      LOG.info("Remove from group: " + groupname + ", username: " + username + ", result: " + result);
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#create(java.lang.String)
   */
  @Override
  public boolean create(String name) {
    Boolean result = null;
    try {
      result = this.accessProvider.create(name);
    } finally {
      LOG.info("Create name: " + name + ", result: " + result);
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#flushCaches()
   */
  @Override
  public void flushCaches() {
    LOG.info("flushCaches");
    this.accessProvider.flushCaches();
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#handles(java.lang.String)
   */
  @Override
  public boolean handles(String name) {
    Boolean result = null;
    try {
      result = this.accessProvider.handles(name);
    } finally {
      LOG.info("Handles name: " + name + ", result: " + result);
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#init(java.util.Properties)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean init(Properties properties) {
    Boolean result = null;
    try {
      result = this.accessProvider.init(properties);
    } finally {
      StringBuilder logMessage = new StringBuilder("init, properties: ");
      if (properties == null) {
        logMessage.append("null");
      } else {
        for (String propertyName : (Set<String>)(Object)properties.keySet()) {
          logMessage.append(propertyName).append(": ").append(properties.get(propertyName)).append(", ");
        }
      }
      LOG.info(logMessage.append(", result: " + result).toString());
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#list()
   */
  @Override
  public List<String> list() {
    List<String> result = null;
    try {
      result = this.accessProvider.list();
    } finally {
      StringBuilder logMessage = new StringBuilder("list: ");
      if (result == null) {
        logMessage.append("null");
      } else {
        for (int i=0;i<result.size();i++) {
          logMessage.append(result.get(i));
          if (i < result.size()) {
            logMessage.append(", ");
          }
        }
      }
      LOG.info(logMessage.toString());
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#load(java.lang.String, com.opensymphony.user.Entity.Accessor)
   */
  @Override
  public boolean load(String name, Accessor accessor) {
    Boolean result = null;
    try {
      result = this.accessProvider.load(name, accessor);
    } finally {
      LOG.info("load name: " + name + ", accessor: " + accessor + (accessor == null ? "" : (", entity: " + accessor.getEntity())) + ", result: " + result);
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#remove(java.lang.String)
   */
  @Override
  public boolean remove(String name) {
    Boolean result = null;
    try {
      result = this.accessProvider.remove(name);
    } finally {
      LOG.info("remove name: " + name + ", result: " + result);
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#store(java.lang.String, com.opensymphony.user.Entity.Accessor)
   */
  @Override
  public boolean store(String name, Accessor accessor) {
    Boolean result = null;
    try {
      result = this.accessProvider.store(name, accessor);
    } finally {
      LOG.info("store name: " + name + ", accessor: " + accessor + (accessor == null ? "" : (", entity: " + accessor.getEntity())) + ", result: " + result);
    }
    return result;
  }

}
