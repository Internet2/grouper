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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.opensymphony.user.Entity.Accessor;
import com.opensymphony.user.provider.CredentialsProvider;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * implement the opensymphony interface that Atlassian uses for products like jira/confluence
 */
@SuppressWarnings("serial")
public class GrouperCredentialsProvider implements CredentialsProvider {

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperCredentialsProvider.class);

  /**
   * @see com.opensymphony.user.provider.UserProvider#create(java.lang.String)
   */
  @Override
  public boolean create(String name) {
    LOG.error("You cannot create here '" + name + "', information is read from the source system via Grouper: " + name);
    throw new RuntimeException("You cannot create here '" + name + "', information is read from the source system via Grouper");
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#flushCaches()
   */
  @Override
  public void flushCaches() {
    LOG.debug("flushCaches");
    new GrouperAccessProvider().flushCaches();
  }

  /**
   * this should return true if user exists, false if not
   * @see com.opensymphony.user.provider.UserProvider#handles(java.lang.String)
   */
  @Override
  public boolean handles(String username) {
    
    String operation = "handles";

    return handlesHelper(username, operation);
  }

  /**
   * helper method for handles
   * @param username
   * @param operation
   * @return if it handles
   */
  private boolean handlesHelper(String username, String operation) {
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new HashMap<String, Object>();

    debugMap.put("operation", operation);
    debugMap.put("username", username);
    try {
      //if we handle it, the user should really be a user at this point...
      String atlassianUsersGroupName = GrouperAtlassianConfig.grouperAtlassianConfig().getAtlassianUsersGroupName();
      debugMap.put("atlassianUsersGroupName", atlassianUsersGroupName);
      boolean result = new GrouperAccessProvider().inGroup(username, atlassianUsersGroupName);
      debugMap.put("result", result);
      return result;
    } catch (RuntimeException re) {
      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;

    }
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#init(java.util.Properties)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean init(Properties properties) {
    //nothing to do here
    Boolean result = true;
    if (LOG.isDebugEnabled()) {
      StringBuilder logMessage = new StringBuilder("init, properties: ");
      if (properties == null) {
        logMessage.append("null");
      } else {
        for (String propertyName : (Set<String>)(Object)properties.keySet()) {
          logMessage.append(propertyName).append(": ").append(properties.get(propertyName)).append(", ");
        }
      }
      logMessage.append(", result: ").append(result);
      LOG.debug(logMessage);
    }
    return result;
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#list()
   */
  @Override
  public List<String> list() {

    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "list");

    List<String> resultList = null;

    try {

      //get this list from the users list
      String atlassianUsersGroupName = GrouperAtlassianConfig.grouperAtlassianConfig().getAtlassianUsersGroupName();
      debugMap.put("atlassianUsersGroupName", atlassianUsersGroupName);
      resultList = new GrouperAccessProvider().listUsersInGroup(atlassianUsersGroupName);
      
      GrouperAtlassianUtils.addToDebugMap(debugMap, resultList, "resultList");
      
      if (LOG.isDebugEnabled()) {
        GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
        LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
      }
      
      return resultList;
    } catch(RuntimeException re) {

      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.error("Error: " + GrouperAtlassianUtils.mapForLog(debugMap), re);
      throw re;
    }
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#load(java.lang.String, com.opensymphony.user.Entity.Accessor)
   */
  @Override
  public boolean load(String username, Accessor accessor) {
    String operation = "load";

    return handlesHelper(username, operation);

  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#remove(java.lang.String)
   */
  @Override
  public boolean remove(String name) {
    LOG.error("You cannot remove here: '" + name + "', information is read from the source system via Grouper: " + name);
    throw new RuntimeException("You cannot remove here '" + name + "', information is read from the source system via Grouper");
  }

  /**
   * @see com.opensymphony.user.provider.UserProvider#store(java.lang.String, com.opensymphony.user.Entity.Accessor)
   */
  @Override
  public boolean store(String name, Accessor accessor) {

    LOG.error("You cannot store here: '" + name + "', information is read from the source system via Grouper");
    throw new RuntimeException("You cannot store here '" + name + "', information is read from the source system via Grouper.");

  }

  /**
   * 
   */
  @Override
  public boolean authenticate(String user, String pass) {

    LOG.error("You cannot authenticate here: '" + user + "', information is read from the source system via Grouper");
    throw new RuntimeException("You cannot authenticate here '" + user + "', information is read from the source system via Grouper.");
  }

  /**
   * 
   */
  @Override
  public boolean changePassword(String user, String pass) {

    LOG.error("You cannot changePassword here: '" + user + "'");
    throw new RuntimeException("You cannot changePassword here '" + user + "'");
  }

}
