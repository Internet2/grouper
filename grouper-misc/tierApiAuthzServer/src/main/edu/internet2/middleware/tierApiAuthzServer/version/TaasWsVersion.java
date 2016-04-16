/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
package edu.internet2.middleware.tierApiAuthzServer.version;

import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;



/**
 * WS grouper version utils
 * @author mchyzer
 *
 */
public enum TaasWsVersion {
  
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
  public static TaasWsVersion serverVersion() {
    return v1;
  }

  
  /** current client version */
  public static ThreadLocal<TaasWsVersion> currentClientVersion = new ThreadLocal<TaasWsVersion>();

  /**
   * put the current client version
   * @param clientVersion
   * @param warnings 
   */
  public static void assignCurrentClientVersion(TaasWsVersion clientVersion, StringBuilder warnings) {
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
  public static TaasWsVersion retrieveCurrentClientVersion() {
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
  public static TaasWsVersion valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {
    return StandardApiServerUtils.enumValueOfIgnoreCase(TaasWsVersion.class, string, exceptionOnNotFound);
  }

}
