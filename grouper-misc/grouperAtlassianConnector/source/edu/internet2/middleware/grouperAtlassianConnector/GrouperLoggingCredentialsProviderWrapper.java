/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.opensymphony.user.Entity.Accessor;
import com.opensymphony.user.provider.CredentialsProvider;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * delegate to another credentials provider
 */
@SuppressWarnings("serial")
public class GrouperLoggingCredentialsProviderWrapper implements CredentialsProvider {

  /** temporary delegate */
  private CredentialsProvider credentialsProvider = null;
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  public GrouperLoggingCredentialsProviderWrapper() {
    
    String credentialsProviderClassName = GrouperClientUtils.propertiesValue("atlassian.logging.accessProvider.class", true);
    Class<CredentialsProvider> credentialsProviderClass = GrouperClientUtils.forName(credentialsProviderClassName);
    this.credentialsProvider = GrouperClientUtils.newInstance(credentialsProviderClass);
    
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperLoggingCredentialsProviderWrapper.class);

  /**
   * @see com.opensymphony.user.provider.UserProvider#create(java.lang.String)
   */
  @Override
  public boolean create(String name) {
    Boolean result = null;
    try {
      result = this.credentialsProvider.create(name);
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
    this.credentialsProvider.flushCaches();
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#handles(java.lang.String)
   */
  @Override
  public boolean handles(String name) {
    Boolean result = null;
    try {
      result = this.credentialsProvider.handles(name);
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
      result = this.credentialsProvider.init(properties);
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
      result = this.credentialsProvider.list();
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
      result = this.credentialsProvider.load(name, accessor);
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
      result = this.credentialsProvider.remove(name);
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
      result = this.credentialsProvider.store(name, accessor);
    } finally {
      LOG.info("store name: " + name + ", accessor: " + accessor + (accessor == null ? "" : (", entity: " + accessor.getEntity())) + ", result: " + result);
    }
    return result;
  }

  /**
   * @see CredentialsProvider#authenticate(String, String)
   */
  @Override
  public boolean authenticate(String name, String password) {
    Boolean result = null;
    try {
      result = this.credentialsProvider.authenticate(name, password);
    } finally {
      LOG.info("authenticate name: " + name + ", result: " + result);
    }
    return result;
  }
  
  /**
   * @see CredentialsProvider#changePassword(String, String)
   */
  @Override
  public boolean changePassword(String name, String password) {

    Boolean result = null;
    try {
      result = this.credentialsProvider.changePassword(name, password);
    } finally {
      LOG.info("changePassword name: " + name + ", result: " + result);
    }
    return result;
  }

}
