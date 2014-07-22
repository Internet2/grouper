/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.permissions;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * bean that has helper methods to process the permissions
 * @author mchyzer
 *
 */
public class PermissionResult {

  /**
   * constructor
   */
  public PermissionResult() {
    
  }
  
  /**
   * permission entries to process
   */
  private Set<PermissionEntry> permissionEntries;

  /**
   * 
   * @param thePermissionEntries
   */
  public PermissionResult(Set<PermissionEntry> thePermissionEntries) {
    this.permissionEntries = thePermissionEntries;
  }

  /**
   * get allowed extensions for this action
   * @return the extensions allowed, never null
   */
  public Set<String> permissionNameExtensions() {
    
    Set<String> extensions = new TreeSet<String>();
    if (this.permissionEntries != null) {
      
      for (PermissionEntry permissionEntry : this.permissionEntries) {
        
        if (permissionEntry.isAllowedOverall()) {
          extensions.add(GrouperUtil.extensionFromName(permissionEntry.getAttributeDefNameName()));
        }
      }
    }
    return extensions;
  }

  /**
   * get allowed extensions for this action
   * @param stemName
   * @param action
   * @param subject
   * @param scope
   * @return the extensions allowed, never null
   */
  public Set<String> permissionNameExtensions(String stemName, String action, Subject subject, Scope scope) {
    
    Set<String> extensions = new TreeSet<String>();
    
    if (this.permissionEntries != null) {
      
      for (PermissionEntry permissionEntry : this.permissionEntries) {
        
        if (permissionEntry.isAllowedOverall() && StringUtils.equals(action, permissionEntry.getAction())) {
          extensions.add(GrouperUtil.extensionFromName(permissionEntry.getAttributeDefNameName()));
        }
      }
    }
    return extensions;
  }
  
}
