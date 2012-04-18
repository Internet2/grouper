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
package edu.internet2.middleware.grouper.permissions.limits;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * innocuous methods that can be used from EL.  Make sure there are no security problems where
 * bad users can do bad things
 * 
 * @author mchyzer
 *
 */
public class LimitElUtils {

  /**
   * see if the labels in the config contain any of the labels for the user
   * @param labelsForUser comma separated, and trimmed
   * @param labelsInConfig comma separated, and trimmed
   * @return true if contains
   */
  public static boolean labelsContain(String labelsForUser, String labelsInConfig) {
    //its ok if the labels for the user are blank
    if (StringUtils.isBlank(labelsForUser)) {
      return false;
    }
    if (StringUtils.isBlank(labelsInConfig)) {
      throw new RuntimeException("Why are labels in config blank?");
    }
    
    //get the labelsInConfig in a set
    Set<String> labelsInConfigSet = GrouperUtil.splitTrimToSet(labelsInConfig, ",");
    
    String[] labelsForUserArray = GrouperUtil.splitTrim(labelsForUser, ",");
    
    for (String labelforUser : labelsForUserArray) {
      if (labelsInConfigSet.contains(labelforUser)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * see if an ip address is on a network
   * 
   * @param ipString
   *          is the ip address to check
   * @param networkIpString
   *          is the ip address of the network
   * @param mask
   *          is the length of the mask (0-32)
   * @return boolean
   */
  public static boolean ipOnNetwork(String ipString, String networkIpString, int mask) {
    return GrouperUtil.ipOnNetwork(ipString, networkIpString, mask);
  }

  /**
   * see if an ip address is on a network
   * 
   * @param ipString
   *          is the ip address to check
   * @param networkIpStrings
   *          are the ip addresses of the networks, e.g. 1.2.3.4/12, 2.3.4.5/24
   * @return boolean
   */
  public static boolean ipOnNetworks(String ipString, String networkIpStrings) {
    return GrouperUtil.ipOnNetworks(ipString, networkIpStrings);
  }

  /**
   * if the ip address is on the realm name in the grouper.properties
   * in grouper.permissions.limits.realm.realmName
   * @param ipAddress 
   * @param realmName 
   * @return if the ip address is on the realm name in the grouper.properties
   */
  public static boolean ipOnNetworkRealm(String ipAddress, String realmName) {

    if (StringUtils.isBlank(realmName) || !realmName.matches("^[a-zA-Z0-9_]+$")) {
      throw new RuntimeException("You must use a realm name which is alphanumeric or underscore... '" + realmName + "'");
    }

    String grouperConfigPropertyName = "grouper.permissions.limits.realm." + realmName;
    String networkIpStrings = GrouperConfig.getProperty(grouperConfigPropertyName);

    if (StringUtils.isBlank(networkIpStrings)) {
      throw new RuntimeException("You should have a grouper.properties entry for " + grouperConfigPropertyName);
    }

    return ipOnNetworks(ipAddress, networkIpStrings);
  }

}
