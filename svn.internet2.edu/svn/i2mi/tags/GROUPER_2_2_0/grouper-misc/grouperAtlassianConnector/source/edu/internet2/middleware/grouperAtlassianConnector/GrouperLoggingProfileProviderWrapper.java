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

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.Entity.Accessor;
import com.opensymphony.user.provider.ProfileProvider;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 *
 */
public class GrouperLoggingProfileProviderWrapper implements ProfileProvider {

  /** temporary delegate */
  private ProfileProvider profileProvider = null;
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  public GrouperLoggingProfileProviderWrapper() {
    
    String accessProviderClassName = GrouperClientUtils.propertiesValue("atlassian.logging.profileProvider.class", true);
    Class<ProfileProvider> accessProviderClass = GrouperClientUtils.forName(accessProviderClassName);
    this.profileProvider = GrouperClientUtils.newInstance(accessProviderClass);
    
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperLoggingProfileProviderWrapper.class);

  /**
   * @see com.opensymphony.user.provider.UserProvider#create(java.lang.String)
   */
  @Override
  public boolean create(String name) {
    Boolean result = null;
    try {
      result = this.profileProvider.create(name);
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
    this.profileProvider.flushCaches();
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#handles(java.lang.String)
   */
  @Override
  public boolean handles(String name) {
    Boolean result = null;
    try {
      result = this.profileProvider.handles(name);
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
      result = this.profileProvider.init(properties);
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
      result = this.profileProvider.list();
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
      result = this.profileProvider.load(name, accessor);
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
      result = this.profileProvider.remove(name);
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
      result = this.profileProvider.store(name, accessor);
    } finally {
      LOG.info("store name: " + name + ", accessor: " + accessor + (accessor == null ? "" : (", entity: " + accessor.getEntity())) + ", result: " + result);
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.ProfileProvider#getPropertySet(java.lang.String)
   */
  @Override
  public PropertySet getPropertySet(String name) {
    PropertySet result = null;
    try {
      result = this.profileProvider.getPropertySet(name);
    } finally {
      StringBuilder stringBuilder = new StringBuilder("getPropertySet: ");
      if (result == null) {
        stringBuilder.append("null");
      } else {
        Collection keys = result.getKeys();
        if (keys == null) {
          stringBuilder.append("keys is null");
        } else if (keys.size() == 0) {
          stringBuilder.append("keys is empty");
        } else {
          for (Object key : keys) {
            String keyString = (String)key;
            Object value = result.getAsActualType(keyString);
            stringBuilder.append(keyString).append(": ").append(value).append(", ");
          }
        }
        
      }
      LOG.info(stringBuilder);
    }
    return result;
  }

  
  
}
