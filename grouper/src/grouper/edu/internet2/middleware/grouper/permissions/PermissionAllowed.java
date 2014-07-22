/**
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
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.permissions;


/**
 * if allowed or disallowed
 */
public enum PermissionAllowed {
  
  /** normal assignment if allowed */
  ALLOWED {

    @Override
    public boolean isDisallowed() {
      return false;
    }
  },
  
  /** disallow underneath an inherited allow to limit the scope of the allow */
  DISALLOWED {

    @Override
    public boolean isDisallowed() {
      return true;
    }
  };
  
  /**
   * if disallowed
   * @return true or false
   */
  public abstract boolean isDisallowed();
  
  /**
   * convert from disallowed boolean to the enum
   * @param disallowed
   * @return the permission allowed
   */
  public static PermissionAllowed fromDisallowedBoolean(boolean disallowed) {
    return disallowed ? DISALLOWED : ALLOWED;
  }
  
}
