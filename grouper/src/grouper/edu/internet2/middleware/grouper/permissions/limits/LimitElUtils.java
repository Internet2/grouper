package edu.internet2.middleware.grouper.permissions.limits;

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
