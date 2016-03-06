package edu.internet2.middleware.tierApiAuthzServer.version;

import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;



/**
 * WS grouper version utils
 * @author mchyzer
 *
 */
public enum AsasWsVersion {
  
  /** the first version available */
  v1;
  
  /** 
   * current version
   * this must be two integers separated by dot for version, and build number.
   * update this before each
   * non-release-candidate release (e.g. in preparation for it)
   * e.g. 1.5
   */
  public static final String ASAS_VERSION = "1.0";

  /**
   * current grouper version
   * @return current grouper version
   */
  public static AsasWsVersion serverVersion() {
    return v1;
  }

  
  /** current client version */
  public static ThreadLocal<AsasWsVersion> currentClientVersion = new ThreadLocal<AsasWsVersion>();

  /**
   * put the current client version
   * @param clientVersion
   * @param warnings 
   */
  public static void assignCurrentClientVersion(AsasWsVersion clientVersion, StringBuilder warnings) {
    currentClientVersion.set(clientVersion);
  }
  
  /**
   * put the current client version
   */
  public static void removeCurrentClientVersion() {
    currentClientVersion.remove();
  }

  /**
   * return current client version or null
   * @return the current client version or null
   */
  public static AsasWsVersion retrieveCurrentClientVersion() {
    return currentClientVersion.get();
  }

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception on not found
   * @return the enum or null or exception if not found
   * @throws AsasRestInvalidRequest problem
   */
  public static AsasWsVersion valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {
    return StandardApiServerUtils.enumValueOfIgnoreCase(AsasWsVersion.class, string, exceptionOnNotFound);
  }

}
